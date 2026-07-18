@echo off
set JAR=C:\Users\XxRaay\.gradle\caches\modules-2\files-2.1\net.neoforged\neoforge\21.1.235\7078cb8452f195826f04c0ab0c4768393e8093db\neoforge-21.1.235-universal.jar
javac -cp "%JAR%" ReflectClass.java
java -cp ".;%JAR%" ReflectClass
