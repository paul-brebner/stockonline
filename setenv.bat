@rem ===============================================================
@rem This batch file is to set environment vairobles for stockonline 
@rem
@rem Usage: setenv
@rem 
@rem ===============================================================

echo off

@rem
@rem You may need to change the following settings for your environment.
@rem

@rem
@rem Your Java
@rem
set JAVA_HOME=D:\app\java\j2sdk1.4.1_01
set path=%JAVA_HOME%\bin;%path%

@rem
@rem Your ant
@rem
set ANT_HOME=C:\usr\ant\jakarta-ant-1.4.1
set path=%ANT_HOME%\bin;%path%


@rem
@rem Your work_home
@rem
set WORK_HOME=D:\user\shiping\project\EJB_common_base_code\2.0\common_base_code
set classpath=.

@rem
@rem Your JDBC driver (We use oracle for example)
@rem
set classpath=%classpath%;%WORK_HOME%\lib\oracle\classes12.zip

@rem
@rem Your j2EE stuff (We use JBoss for example)
@rem
set classpath=%classpath%;%WORK_HOME%\lib\jboss\jboss-j2ee.jar
set classpath=%classpath%;%WORK_HOME%\lib\jboss\jnpserver.jar
set classpath=%classpath%;%WORK_HOME%\lib\jboss\jboss.jar

@rem
@rem Your client
@rem
set classpath=%classpath%;%WORK_HOME%\build\classes
