@echo off
echo ##################################################
echo ###�ýű��������õ�����http��httpsЭ���git����###
echo ###             power by zengyufei             ###
echo ##################################################
echo __

rem ��������
set gitusername=
set gitpassword=
set /p gitusername=������username��
set /p gitpassword=������password��

rem ��������
echo __
echo https://%gitusername%:%gitpassword%@github.com > %USERPROFILE%/.git-credentials
git config --global credential.helper store
echo 'Sucess!'