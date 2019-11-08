cd /d D:\Software\Jenkins
jenkins.exe stop
timeout 4
RD /S /Q D:\Software\Jenkins\plugins\iTMS-Cucumber-Report-Publisher
cd /d D:\Software\Jenkins\plugins
del /f iTMS-Cucumber-Report-Publisher.hpi
timeout 3