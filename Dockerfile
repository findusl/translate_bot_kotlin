FROM openjdk:15-jdk
# EXPOSE 8080:8080
RUN mkdir /app
COPY ./build/install/translate_bot_kotlin/ /app/
WORKDIR /app/bin
CMD ["./translate_bot_kotlin"]
# this is probably needed when calling docker run -v /volume1/docker:/data
# /Users/me/StudioProjects/translate_bot_kotlin:/data
