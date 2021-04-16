[![Kotlin CI with Gradle](https://github.com/findusl/translate_bot_kotlin/actions/workflows/gradle.yml/badge.svg)](https://github.com/findusl/translate_bot_kotlin/actions/workflows/gradle.yml)

# translate_bot_kotlin

Just a little project where I try out Kotlin. Maybe I'm going to complete the bot maybe not. I'll see

Next steps:
 - Automate deployment to docker on server (jenkins?)
 - Write tests and setup github actions that run those tests automatically
 - See if I can improve performance. GraalVM maybe?

## Deploy with Docker:

1. Run `gradle installDist` to generate the necessary files. 
   The result will be located in `build/install/translate_bot_kotlin`
2. Run `docker build -t translate_bot_kotlin .` to generate the docker container.
3. Now run the container. In the following command you have to insert the path where the settings.json should be saved. 
   `docker run -v <directory of settings file>:/data translate_bot_kotlin`

Alternatively after Step 3 one can save the docker container to deploy it somewhere else, using 
`docker save --output translate_bot_kotlin.tar translate_bot_kotlin`

## Creating native image:

Install GraalVM and install native image. Add to path using 
`export PATH=/Library/Java/JavaVirtualMachines/graalvm-ce-java11-21.0.0.2/Contents/Home/bin:$PATH`.
Create jar by running `gradle jarWithDependencies` (Or just `gradle jar` until the other task is fixed).
Then run `native-image -jar build/libs/translate_bot_kotlin-1.1-SNAPSHOT.jar -H:Name=translate_bot_kotlin`.
Not yet working, because of some coroutines stuff.
Try out `--target` and see if it works somehow with `Linux x86`
