# WikiApp

# Description

A website that helps artists communicate a world to an audience as well
as provide organization tools for world building.

# How it solves the problem

A big problem artists have on Twitter is posting their original characters (OCs)
and the audience not knowing the context behind the character. This restricts the
artist into delivering only art that can only be enjoyed with limited context.

This website solves it
by letting the author attach characters to the post. If the audience wants to find
out more about the characters, they can click on links on the side, bringing up a
wiki page. The wiki page can be contributed by the audience. These character and
setting pages make up a "World".

Furthermore, the author can build timelines through their posts. The artist can upload
and insert their upload chronologically among their other uploads. Also, the artist
can link or "reply" left, down, or right. This builds an alternative timeline.

# Features

-   Users

    -   Users oauth login with Discord or Google only.
    -   There is only 1 non-Oauth account, the root account for administration.

-   Security

    -   Sessions and CSRF
    -   Ban users
    -   Do not allow links in submissions unless they are from authorized domains.

-   ! Like + React to user submissions
-   Comic ratings
-   Follow users
-   Notifications
-   Email notifications
-   Block users
-   User profiles
-   ! Create articles
    -   Create character articles, with details about a character. Contains history sections.
    -   Create timeline articles with info about a world/timeline.
    -   Can attach characters to worlds.
    -   Comments.
    -   tag articles
    -   replies
-   ! Create art submissions
    -tags
    -replies
-   Create art comics
    -tags
    -replies
- ! create optional timelines that the articles, comics, or characters belong to and organize them
chronologically according to your canon.
- CRUD all resources.

# Software Architecture

This app uses a domain driven design.

- Data Transfer Object (DTO) Layer
  - Objects that store data coming in to and from outside the server.
- Controller Layer: 
  - Responsible for taking in JSON or multipart data from the client
  - Validates data coming in
  - Sends out data from services.
  - handles auth
- Service Layer:
  - handles any business logic on data coming in from the client
  and stores it.
  - handles ownership authorization.
- Repository Layer:
  - Serves as a middleware between the Service layer and the DAO.
  - Collates data such as nested objects so it can deliver complete data to the server.
  - Handles transactions for multiple Models.
  - Hides DB implementation details from the Service layer.
- Data Access Object (DAO) Layer:
  - Hides ugly SQL queries away.
  - Maps data to and from Model objects.
- Model:
  - Object representing a table row from our DB.


# Data Structure

## Timeline

Each timeline starts a submission article tree and owns all the wiki articles.

## Articles

The basic unit of articles on the website.

### Submission Article

### Wiki Articles

### Wiki Character Articles

### Wiki Item Articles

### Wiki Setting Articles

### User
