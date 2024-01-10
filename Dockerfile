FROM ubuntu:latest
LABEL authors="Mercer"

ENTRYPOINT ["top", "-b"]