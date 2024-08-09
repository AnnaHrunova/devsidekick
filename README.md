# DEV SIDEKICK

Dev Sidekick is a gateway between "push the code" button and regular project state tracking.

## Features

Dev Sidekick fetches all commits made to Git repository per day and with the help of Gemini API converts them to human-readable summary for later usage.  
At the current state of implementation later usage includes:  
&nbsp;&nbsp;&nbsp;&nbsp; 1. Project progress tracking system (brief summary of what was done and when);  
&nbsp;&nbsp;&nbsp;&nbsp; 2. Project code summary comparison with Business Requirements document with the help of Gemini API;  
#### Which problems are solved?
1. Project manager can track the state of feature implementation on a high level without additional clarification with developers team;
2. Missing features or any result of miscommunication can be detected automatically and as fast as possible;  
3. Developer can get enough context to start or pick up a task;  
4. New team member onboarding process may be simplified;  
5. Significantly reduces time spent to investigate changes with unclear commit messages;  
6. Significantly reduces time spent to clarifications regarding reason/author/time of done/undone changes.  

#### Future possible improvements/development
1. Integrate with other Git systems (GitHub is used in MVP, but other Git systems like GitLab, Bitbucket, etc. may be integrated as well);  
2. Add different options to provide Business Requirements documents (not only local and Google Docs);  
3. Categorize differences detection between code summary and requirements in order to provide some kind of alerting about occurred conflicts.  


## Tools

- Java, [JDK 22](https://sdkman.io/jdks#amzn)
- Spring Boot 3
- Gradle (wrapper)
- Gemini API
- Google Docs Api
- Thymeleaf (for user interface)
- [EnvFile plugin](https://plugins.jetbrains.com/plugin/7861-envfile)


## Project Structure

```

devsidekick/
├── src/
│   ├── main/
│   │   ├── java/com/gemini/devsidekick/
│   │   │   ├── config/           # Project configuration files
│   │   │   ├── controller/       # REST controller for API endpoints
│   │   │   ├── model/            # Data models
│   │   │   └── service/          # Business logic layer
│   │   └── resources/
│   │       ├── doc/                      # Business requirements .txt file to be used if Google Docs API is launched in mock mode
│   │       ├── doc/history/              # Saved code summaries created by Gemini
│   │       ├── static/css/               # Stylesheet for user inteface
│   │       ├── templates/                # Templates used to render user interface
│   │       ├── credentials-example.json  # Credentials file example for Google Docs access
│   │       ├── credentials.json          # Credentials for Google Docs access (should not be committed, added to .gitignore)
│   │       └── application.yaml          # Main properties file of the project
│   └── test/
│       └── java/com/gemini/              # TBD
├── .env                    # Environment variables based on .env-init and adjusted for local usage (should not be committed, added to .gitignore)
├── .env-init               # List of possible environment variables to configure
└── build.gradle            # Gradle build script


```

## High Level Implementation

### Happy Path


## Detailed Implementation

DevSidekick consists of 2 core components: HistoryScheduler and OperationsController.  

> **HistoryScheduler**  
> Responsibility:  
> 1. fetch code changes from the project's GitHub repository
> 2. request Gemini API to analyze fetched code changes
> 3. save response to file for later usage

** By default, runs with 24h rate in order to process changed made to the code repository on daily basis.  
** In addition, supports configurable (via .env file) start- and end- history analysis dates providing flexible code summary reports.  
Note: In order to save resources, check for code summary file existence is performed. No action is taken if code changes are already analyzed.  
<br/>
<br/>

> **OperationController**  
> Responsibility:
> 1. return code changes history  
   **Live mode off (recommended\*):** history is returned from files created by HistoryScheduler  
   **Live mode on (not recommended\*\*):**  
> &nbsp;&nbsp;&nbsp;&nbsp;1. makes a request to GitHub for every selected day in the range  
> &nbsp;&nbsp;&nbsp;&nbsp;2. requests Gemini to analyze code changes  
> &nbsp;&nbsp;&nbsp;&nbsp;3. displays response from Gemini in live mode
> 2. compare code summary with Business requirements document  
   **Live mode off (recommended for local run\*\*\*):** code summary is compared with the Business Requirements stored in the project sources   
   **Live mode on (recommended for deployment\*\*\*\*):** code summary is compared with the Business Requirements document fetched from Google Docs

*In standard application flow HistoryScheduler works every day saving code summary on daily basis.  
Excluding special cases (when git history was changed), live mode off saves costs and resources by skipping repeatedly calling GeminiA PI and GitHub API.  
**Consumes time resources and costs. Not recommended for regular usage. Recommended for special cases (demos, double-checking saved code summaries).  
***In order to simplify GoogleDocs access set up, local copy of Business Requirements content is used.  
****Google Docs API is used. Requires credentials.json file and granting access to specific user to access requested page.  
Not recommended for local run unless Google Docs API integration is the purpose of development. More info: [authorize_credentials_for_a_desktop_application](https://developers.google.com/docs/api/quickstart/java#authorize_credentials_for_a_desktop_application)

     
## Installation

1. **Clone the repository:**

    ```sh
    git clone https://github.com/AnnaHrunova/devsidekick.git
    cd devsidekick
    ```

2. **Build the project:**

    ```sh
    ./gradlew build
    ```
3. **Create .env file**  
   Create a ``.env`` file in the root of the project in order to define required environment variables  
based on .env-init file example.

4. **(Optional) Add credentials.josn**  
If Google Docs api is launched in live mode, credentials.json file and GOOGLE_DOCS_APP environment variable definition are  required. More info: [authorize_credentials_for_a_desktop_application](https://developers.google.com/docs/api/quickstart/java)

5. **Run the application:**

    ```sh
    ./gradlew bootRun
    ```

6. **Access the app:**  

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;http://localhost:8080

![Alt text](pictures/home.png? "Home")

