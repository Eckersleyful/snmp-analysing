@echo off
powershell.exe Stop-Process -Name "snmpd" -Force
timeout /t 1 /nobreak >nul
sc start "Net-SNMP Agent"
