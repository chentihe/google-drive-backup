# Google Drive Backup

Using google drive api to back up the files periodically.

## Google Drive API Key

1. Navigate to [GCP](https://console.cloud.google.com/)

2. Create a new project

![image](https://user-images.githubusercontent.com/48288259/185822098-4359de6e-22a4-4389-b61a-c03b089ffb0f.png)

3. Enable Google Drive API

![image](https://user-images.githubusercontent.com/48288259/185822188-73783ffd-514f-40f6-bfa2-a3daa23e7278.png)

![image](https://user-images.githubusercontent.com/48288259/185822216-bf2aac89-5cbc-457c-a61b-a4d17f7d3f09.png)

4. Create a API Key

![image](https://user-images.githubusercontent.com/48288259/185822518-bc53443b-21bc-40a1-ab27-1f7d22227d40.png)

5. Download the credentials on your local envirnoment

6. This repo is using service accounts as credentials, so the app can only be executed on local envirnoment

7. You will see an email address which is on behalf of Service Account, just share the files you want to backup to this email address

## Java Setup

1. Put the credentials you created earlier in to `~/project_name/src/main/resources/` 

2. Open the config file on `~/project_name/src/main/resources/googledriveconfig.yml`

3. Change the `rootFolderPath` & `rootFolderByYear` to the directory you want to store the files

4. You can using the script to run this app periodically
