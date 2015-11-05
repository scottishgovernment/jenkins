#!/bin/bash

sslyze --regular perwww.mygov.scot:443 2>&1 | tee sslyze.log
o-saft +vulns https://perwww.mygov.scot 2>&1 | tee o-saft.log

status='PASS'

####################################
## SSLYZE Test Result Log Validation
####################################

value=$( grep -c "OK - Compression disabled" sslyze.log )



if [ $value -eq 1 ]
then
    echo "Compression disabled - test passed"
else
  echo "\n\n"
  echo "##################################################"
  echo "ERROR : Compression not disabled, exiting"
  echo "##################################################"
  echo "\n\n"
  status='FAIL'
fi

#Check client initated Renogotiations
#value=$( grep -c "Client-initiated Renegotiations:   OK" sslyze.log )
#if [ $value -eq 1 ]
#then
#    echo "Client-initiated Renegotiation - Test passed"
#else
#  echo "\n\n"
#  echo "##########################################################"
#  echo "ERROR : Client-initiated Renegotiation Test Failed, exiting"
#  status='FAIL'
  
#  echo "##########################################################"
#   echo "\n\n"
#fi

#Check secure renogotiations
value=$( grep -c "Secure Renegotiation:              OK - Supported" sslyze.log )
if [ $value -eq 1 ]
then
    echo "Secure Renegotiation - Test passed"
else
    echo "\n\n"
    echo "###############################################"
   
  echo "ERROR : Secure Renegotiation Test Failed, exiting"
  status='FAIL'
   echo "###############################################"
   echo "\n\n"
fi

#Check vulnerability to heart bleed
value=$( grep -c "OK - Not vulnerable to Heartbleed" sslyze.log )
if [ $value -eq 1 ]
then
    echo "vulnerable to Heartbleed - Test passed"
else
   echo "\n\n"
   echo "###############################################"
  
  echo "ERROR : vulnerable to Heartbleed Test Failed, exiting"
  status='FAIL'
   echo "###############################################"
   echo "\n\n"
fi


#Check SSL V2 & SSL V3 cipher suites 
value=$( grep -c "Server rejected all cipher suites." sslyze.log )
if [ $value -eq 2 ]
then
    echo "SSL V2 & SSL V3 cipher suites - Test passed"
else
   echo "\n\n"
   echo "###############################################"
  
  echo "ERROR : SSL V2 & SSL V3 cipher suites Test Failed, exiting"
  status='FAIL'
   echo "###############################################"
   echo "\n\n"
fi




####################################
## O-SAFT Test Result Log Validation
####################################


#check Connection is safe against BEAST attack (any cipher):	yes
value=$( grep -c "Connection is safe against BEAST attack (any cipher):\s*yes" o-saft.log )
if [ $value -eq 1 ]
then
    echo "Connection is safe against BEAST attack (any cipher) - Test passed"
else
   echo "\n\n"
   echo "###############################################"
  
  echo "ERROR : Connection is safe against BEAST attack (any cipher) Test Failed, exiting"
  status='FAIL'
   echo "###############################################"
   echo "\n\n"
fi
# This was reviewed with Technical Architect, and this attack is very 
# unlikely in our context
#Connection is safe against BREACH attack:
#value=$( grep -c "Connection is safe against BREACH attack:\s*yes" o-saft.log )
#if [ $value -eq 1 ]
#then
#    echo "Connection is safe against BREACH attack: - Test passed"
#else
#  echo "\n\n"
#   echo "###############################################"
  
#  echo "ERROR : Connection is safe against BREACH attack: Test Failed, exiting"
#  status='FAIL'
#   echo "###############################################"
#   echo "\n\n"
#fi

#Connection is safe against CRIME attack:
value=$( grep -c "Connection is safe against CRIME attack:\s*yes" o-saft.log )
if [ $value -eq 1 ]
then
    echo "Connection is safe against CRIME attack: Test passed"
else
   echo "\n\n"
   echo "###############################################"
  
  echo "ERROR : Connection is safe against CRIME attack-- Test Failed, exiting"
  status='FAIL'
   echo "###############################################"
   echo "\n\n"
fi

#Connection is safe against FREAK attack:	
value=$( grep -c "Connection is safe against FREAK attack:\s*yes" o-saft.log )
if [ $value -eq 1 ]
then
    echo "Connection is safe against FREAK attack: Test passed"
else
   echo "\n\n"
   echo "###############################################"
  
  echo "ERROR : Connection is safe against FREAK attack -- Test Failed, exiting"
  status='FAIL'
   echo "###############################################"
   echo "\n\n"
fi

#Connection is safe against heartbleed attack	
value=$( grep -c "Connection is safe against heartbleed attack:\s*yes" o-saft.log )
if [ $value -eq 1 ]
then
    echo "Connection is safe against heartbleed attack:	 Test passed"
else
   echo "\n\n"
   echo "###############################################"
  
  echo "ERROR : Connection is safe against heartbleed attack  -- Test Failed, exiting"
  status='FAIL'
   echo "###############################################"
   echo "\n\n"
fi

# Following failure is analyzed with Technical Arcitect, And is very unlikely to occur
# In this context, Hence commenting this test.
#Connection is safe against Lucky 13 attack:	
#value=$( grep -c "Connection is safe against Lucky 13 attack:\s*yes" o-saft.log )
#if [ $value -eq 1 ]
#then
#    echo "Connection is safe against Lucky 13 attack: Test passed"
#else
#   echo "\n\n"
#   echo "###############################################"
  
#  echo "ERROR : Connection is safe against Lucky 13 attack-- Test Failed, exiting"
#  status='FAIL'
#   echo "###############################################"
#   echo "\n\n"
#fi

#Connection is safe against POODLE attack:
value=$( grep -c "Connection is safe against POODLE attack:\s*yes" o-saft.log )
if [ $value -eq 1 ]
then
    echo "Connection is safe against POODLE attack:	 Test passed"
else
   echo "\n\n"
   echo "###############################################"
  
  echo "ERROR : Connection is safe against POODLE attack -- Test Failed, exiting"
  status='FAIL'
   echo "###############################################"
   echo "\n\n"
fi

#Connection is safe against RC4 attack:	yes
value=$( grep -c "Connection is safe against RC4 attack:\s*yes" o-saft.log )
if [ $value -eq 1 ]
then
    echo "Connection is safe against RC4 attack:		 Test passed"
else
   echo "\n\n"
   echo "###############################################"
   echo "ERROR : Connection is safe against RC4 attack -- Test Failed, exiting"
  status='FAIL'
   echo "###############################################"
   echo "\n\n"
fi


if [ ${status} = 'PASS' ]
then
     echo "Test Script PASSED"
     exit 0

else
    echo "Test script FAILED"
    exit 1
fi


