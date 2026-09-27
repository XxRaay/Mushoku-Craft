(function () {
	let exportAction;

	BBPlugin.register('java_rotation_fixer', {
		title: 'Java Rotation Fixer',
		author: 'MushokuCraft',
		version: '2.0.0',
		min_version: '4.8.0',
		description: 'Adds "Export Fixed Java Model" that bakes invalid rotations (>45° or multi-axis) into the exported JSON without modifying your Blockbench project.',
		icon: 'build',
		variant: 'both',

		onload() {
			exportAction = new Action('export_fixed_java_model', {
				name: 'Export Fixed Java Model',
				description: 'Export a Java Block/Item model with invalid rotations baked into coordinates (does not modify your project).',
				icon: 'build',
				click: function () {
					exportFixedModel();
				}
			});
			MenuBar.addAction(exportAction, 'file.export');
		},

		onunload() {
			if (exportAction) exportAction.delete();
		}
	});

	// ---- Constants ----
	const VALID_ANGLES = [-45, -22.5, 0, 22.5, 45];

	function nearestValidAngle(angle) {
		let best = VALID_ANGLES[0];
		let bestDiff = Math.abs(angle - best);
		for (const v of VALID_ANGLES) {
			if (Math.abs(angle - v) < bestDiff) {
				bestDiff = Math.abs(angle - v);
				best = v;
			}
		}
		return best;
	}

	// ---- Rotation helpers ----
	function rotatePoint(point, origin, rx, ry, rz) {
		let p = [point[0] - origin[0], point[1] - origin[1], point[2] - origin[2]];
		if (rx !== 0) {
			const r = (rx * Math.PI) / 180, c = Math.cos(r), s = Math.sin(r);
			p = [p[0], p[1] * c - p[2] * s, p[1] * s + p[2] * c];
		}
		if (ry !== 0) {
			const r = (ry * Math.PI) / 180, c = Math.cos(r), s = Math.sin(r);
			p = [p[0] * c + p[2] * s, p[1], -p[0] * s + p[2] * c];
		}
		if (rz !== 0) {
			const r = (rz * Math.PI) / 180, c = Math.cos(r), s = Math.sin(r);
			p = [p[0] * c - p[1] * s, p[0] * s + p[1] * c, p[2]];
		}
		return [
			Math.round((p[0] + origin[0]) * 1000) / 1000,
			Math.round((p[1] + origin[1]) * 1000) / 1000,
			Math.round((p[2] + origin[2]) * 1000) / 1000
		];
	}

	// Mirror from/to for 180° Y rotation around origin
	function mirror180Y(from, to, origin) {
		const ox = origin[0], oz = origin[2];
		return {
			from: [
				Math.round((2 * ox - to[0]) * 1000) / 1000,
				from[1],
				Math.round((2 * oz - to[2]) * 1000) / 1000
			],
			to: [
				Math.round((2 * ox - from[0]) * 1000) / 1000,
				to[1],
				Math.round((2 * oz - from[2]) * 1000) / 1000
			]
		};
	}

	// Swap face directions for 180° Y rotation
	function swapFaces180Y(faces) {
		const newFaces = {};
		const swap = { north: 'south', south: 'north', east: 'west', west: 'east' };
		for (const [dir, face] of Object.entries(faces)) {
			const newDir = swap[dir] || dir;
			newFaces[newDir] = JSON.parse(JSON.stringify(face));
			// Flip U coordinates for side faces
			if (dir !== 'up' && dir !== 'down') {
				const uv = newFaces[newDir].uv;
				newFaces[newDir].uv = [uv[2], uv[1], uv[0], uv[3]];
			} else {
				const uv = newFaces[newDir].uv;
				newFaces[newDir].uv = [uv[2], uv[3], uv[0], uv[1]];
			}
		}
		return newFaces;
	}

	// ---- Build export JSON from the live Blockbench model ----
	function buildExportJson() {
		// Use Blockbench's own Java exporter as a base, then post-process
		const codec = Codecs.java_block;
		if (!codec) {
			Blockbench.showMessageBox({
				title: 'Error',
				message: 'Java Block/Item codec not found. Make sure you are in Java Block/Item format.',
				icon: 'error'
			});
			return null;
		}

		// Compile the model via standard codec
		const compiled = codec.compile();
		let model;
		try {
			model = typeof compiled === 'string' ? JSON.parse(compiled) : compiled;
		} catch (e) {
			Blockbench.showMessageBox({
				title: 'Error',
				message: 'Failed to parse compiled model: ' + e.message,
				icon: 'error'
			});
			return null;
		}

		if (!model.elements) return model;

		let fixedCount = 0;
		let approxCount = 0;

		for (const elem of model.elements) {
			if (!elem.rotation) continue;

			const rot = elem.rotation;

			// Standard Java rotation format: { angle, axis, origin }
			if (rot.angle !== undefined) {
				if (VALID_ANGLES.includes(rot.angle)) continue; // already valid

				// Invalid angle — approximate
				const nearest = nearestValidAngle(rot.angle);
				rot.angle = nearest;
				if (nearest === 0) delete elem.rotation;
				approxCount++;
				fixedCount++;
				continue;
			}

			// Bedrock-style rotation: { x, y, z, origin }
			if (rot.x !== undefined || rot.y !== undefined || rot.z !== undefined) {
				const rx = rot.x || 0;
				const ry = rot.y || 0;
				const rz = rot.z || 0;
				const origin = rot.origin || [8, 8, 8];

				// Special case: pure 180° Y rotation — mirror coordinates exactly
				if (rx === 0 && rz === 0 && (ry === 180 || ry === -180)) {
					const mirrored = mirror180Y(elem.from, elem.to, origin);
					elem.from = mirrored.from;
					elem.to = mirrored.to;
					elem.faces = swapFaces180Y(elem.faces);
					delete elem.rotation;

					// Fix from < to
					for (let i = 0; i < 3; i++) {
						if (elem.from[i] > elem.to[i]) {
							const tmp = elem.from[i];
							elem.from[i] = elem.to[i];
							elem.to[i] = tmp;
						}
					}
					fixedCount++;
					continue;
				}

				// Pure 90° or 270° Y rotation — also mirrorable
				if (rx === 0 && rz === 0 && (ry % 90 === 0)) {
					const corners = getCorners(elem.from, elem.to);
					const rotated = corners.map(c => rotatePoint(c, origin, 0, ry, 0));
					const newFrom = [
						Math.min(...rotated.map(c => c[0])),
						Math.min(...rotated.map(c => c[1])),
						Math.min(...rotated.map(c => c[2]))
					];
					const newTo = [
						Math.max(...rotated.map(c => c[0])),
						Math.max(...rotated.map(c => c[1])),
						Math.max(...rotated.map(c => c[2]))
					];
					elem.from = newFrom;
					elem.to = newTo;
					delete elem.rotation;
					fixedCount++;
					continue;
				}

				// General case: try to keep one valid axis, approximate the rest
				// Find the dominant non-zero axis
				let dominantAxis = null, dominantAngle = 0;
				if (rz !== 0) { dominantAxis = 'z'; dominantAngle = rz; }
				else if (rx !== 0) { dominantAxis = 'x'; dominantAngle = rx; }
				else if (ry !== 0) { dominantAxis = 'y'; dominantAngle = ry; }

				if (dominantAxis) {
					const nearest = nearestValidAngle(dominantAngle);
					elem.rotation = {
						angle: nearest,
						axis: dominantAxis,
						origin: origin
					};
					if (nearest === 0) delete elem.rotation;
					approxCount++;
				} else {
					delete elem.rotation;
				}
				fixedCount++;
			}
		}

		model._fixStats = { fixed: fixedCount, approx: approxCount };
		return model;
	}

	function getCorners(from, to) {
		return [
			[from[0], from[1], from[2]],
			[to[0],   from[1], from[2]],
			[from[0], to[1],   from[2]],
			[to[0],   to[1],   from[2]],
			[from[0], from[1], to[2]],
			[to[0],   from[1], to[2]],
			[from[0], to[1],   to[2]],
			[to[0],   to[1],   to[2]]
		];
	}

	// ---- Export ----
	function exportFixedModel() {
		const model = buildExportJson();
		if (!model) return;

		const stats = model._fixStats || { fixed: 0, approx: 0 };
		delete model._fixStats;

		const jsonStr = JSON.stringify(model, null, '\t');

		Blockbench.export({
			type: 'JSON Model',
			extensions: ['json'],
			savetype: 'json',
			content: jsonStr,
			name: Project.name || 'model',
		}, (path) => {
			let msg = 'Model exported successfully!';
			if (stats.fixed > 0) {
				msg += `\n\nFixed ${stats.fixed} element(s) with invalid rotations.`;
			}
			if (stats.approx > 0) {
				msg += `\n${stats.approx} angle(s) were approximated to the nearest valid value (-45, -22.5, 0, 22.5, 45).`;
			}
			if (stats.fixed === 0) {
				msg += '\n\nAll rotations were already Java-compatible.';
			}

			Blockbench.showMessageBox({
				title: 'Java Rotation Fixer',
				message: msg,
				icon: 'build'
			});
		});
	}
})();
