;This file will be executed next to the application bundle image
;I.e. current directory will contain folder AutoPrimer3 with application files
[Setup]
AppId={{AutoPrimer3}}
AppName=AutoPrimer3
AppVersion=3.1
AppVerName=AutoPrimer3 3.1
AppPublisher=David A. Parry
AppComments=AutoPrimer3
AppCopyright=Copyright (C) 2015
;AppPublisherURL=http://java.com/
;AppSupportURL=http://java.com/
;AppUpdatesURL=http://java.com/
DefaultDirName={localappdata}\AutoPrimer3
DisableStartupPrompt=Yes
DisableDirPage=Yes
DisableProgramGroupPage=Yes
DisableReadyPage=Yes
DisableFinishedPage=Yes
DisableWelcomePage=Yes
DefaultGroupName=AutoPrimer3
;Optional License
LicenseFile=
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename=AutoPrimer3-3.1
Compression=lzma
SolidCompression=yes
PrivilegesRequired=lowest
SetupIconFile=AutoPrimer3\AutoPrimer3.ico
UninstallDisplayIcon={app}\AutoPrimer3.ico
UninstallDisplayName=AutoPrimer3
WizardImageStretch=No
WizardSmallImageFile=AutoPrimer3-setup-icon.bmp   
ArchitecturesInstallIn64BitMode=x64

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "AutoPrimer3\AutoPrimer3.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "AutoPrimer3\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\AutoPrimer3"; Filename: "{app}\AutoPrimer3.exe"; IconFilename: "{app}\AutoPrimer3.ico"; Check: returnTrue()
Name: "{commondesktop}\AutoPrimer3"; Filename: "{app}\AutoPrimer3.exe";  IconFilename: "{app}\AutoPrimer3.ico"; Check: returnFalse()

[Run]
Filename: "{app}\AutoPrimer3.exe"; Description: "{cm:LaunchProgram,AutoPrimer3}"; Flags: nowait postinstall skipifsilent; Check: returnTrue()
Filename: "{app}\AutoPrimer3.exe"; Parameters: "-install -svcName ""AutoPrimer3"" -svcDesc ""AutoPrimer3"" -mainExe ""AutoPrimer3.exe""  "; Check: returnFalse()

[UninstallRun]
Filename: "{app}\AutoPrimer3.exe "; Parameters: "-uninstall -svcName AutoPrimer3 -stopOnUninstall"; Check: returnFalse()

[Code]
function returnTrue(): Boolean;
begin
  Result := True;
end;

function returnFalse(): Boolean;
begin
  Result := False;
end;

function InitializeSetup(): Boolean;
begin
// Possible future improvements:
//   if version less or same => just launch app
//   if upgrade => check if same app is running and wait for it to exit
//   Add pack200/unpack200 support? 
  Result := True;
end;  
