FROM amazoncorretto:17
# EXPOSE 8080:8080
WORKDIR /app
COPY ./build/install/translate_bot_kotlin/ /app/
WORKDIR /app/bin
ENTRYPOINT ["./translate_bot_kotlin", "/data"]
# this is probably needed when calling docker run -v /volume1/docker:/data
# /Users/me/StudioProjects/translate_bot_kotlin:/data
