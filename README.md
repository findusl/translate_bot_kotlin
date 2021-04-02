# translate_bot_kotlin

Just a little project where I try out Kotlin. Maybe I'm going to complete the bot maybe not. I'll see

Next steps:
 - Persist chats settings to file (configurable via yml)
 - Build docker. 
     - Figure out how to pass secrets.yml files to docker without exposing
 - Deploy (wohoo)
 - Automate deployment to docker on server (jenkins?)
 - Write tests and setup github actions that run those tests automatically

## Deploy with Docker:

1. Run `gradle installDist` to generate the necessary files. 
   The result will be located in `build/install/translate_bot_kotlin`
2. Run `docker build -t translate_bot_kotlin .` to generate the docker container.
3. Now run the container. In the following command you have to insert the path where the settings.json should be saved. 
   `docker run -v <directory of settings file>:/data translate_bot_kotlin`
