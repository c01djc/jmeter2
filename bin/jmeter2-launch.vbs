' Silently re-launch jmeter2.bat without a visible console (GUI mode).
' Called from jmeter2.bat when the user double-clicks for interactive use.
Option Explicit
Dim sh, fso, binDir, bat, args, i, a, cmd
Set sh = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
binDir = fso.GetParentFolderName(WScript.ScriptFullName)
bat = binDir & "\jmeter2.bat"
args = ""
For i = 0 To WScript.Arguments.Count - 1
  a = WScript.Arguments(i)
  If InStr(a, """") > 0 Then a = Replace(a, """", """""")
  If (InStr(a, " ") > 0) Or (InStr(a, "&") > 0) Or (InStr(a, "(") > 0) Then
    a = """" & a & """"
  End If
  args = args & " " & a
Next
' WindowStyle 0 = hidden; False = do not wait
cmd = "cmd /c set JMETER2_HIDDEN=1&& """ & bat & """" & args
sh.Run cmd, 0, False
