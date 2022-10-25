Installation steps :
1. Unzip the Enovia-EntitlementManager-xxxx.zip to C:\
2. The directory stucture should be as below :
	C:\
	----Enovia-EntitlementManager\
						----conf\
						----jpo\
						----logs\
						----setup\						----xlib\						----deploymentconfig_sample.xml
						----nextlabs-enovia-em.jar
						----README.txt(this file)
3. Run the install_JPO.bat from C:\Enovia-EntitlementManager\setup folder, this will install the jpo into Enovia Engine.
   You may need to edit the install.tcl file to reflect the correct enovia user for perform this installation 
   of jpo. The user account shall have the administrator right and access to "eService Administration" vault.
   It should run successfully without any error.4. Run the install_Server.bat to install the EM to 3DSpace.5. Run the install_Studio.bat to install the EM for Matrix Navigator
   
6. Refer to the Enovia EM Admin Guide for further installation.