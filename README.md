# Eno-WS

Major evolution of the web service for the [Eno](https://github.com/InseeFr/Eno/tree/v3-develop) v3 Java library.

The goal of this project is to progressively migrate endpoints to services using Eno v3.

During the transition, the application has two kinds of endopoints:

- Eno "XML" endpoints that call the v1 API (that uses the Eno v2 library), in a transparent way for users.
- Eno "Java" endpoints that call services of the v2 API (using Eno v3 library), with messages to notify the user that the service behind has changed.
