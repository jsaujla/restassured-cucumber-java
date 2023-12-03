@regression
Feature: Playlists

  Background:
    Given An authorized user is available

  Scenario: Verify Playlists. User should be able to create, get and update a playlist
    When With request headers
      | Content-Type     | Authorization  |
      | application/json | {access_token} |
    And With request body: "src/test/resources/test-data/CreatePlaylists.json"
      | name         | description              | public |
      | New Playlist | New playlist description | false  |
    When User makes a POST request to endpoint: "users/{user_id}/playlists"
    Then Response status code should be: 201
    And Response body should match with schema: "src/test/resources/schema/PlaylistSchema.json"


  Scenario Outline: Verify create Playlists with invalid payload
    When With request headers
      | Content-Type     | Authorization  |
      | application/json | {access_token} |
    And With request body: "src/test/resources/test-data/CreatePlaylists.json"
      | name   | description   | public   |
      | <name> | <description> | <public> |
    When User makes a POST request to endpoint: "users/{user_id}/playlists"
    Then Response status code should be: <statusCode>
    And Response body should match with schema: "src/test/resources/schema/ErrorSchema.json"
    Examples:
      | name | description              | public  | statusCode |
      |      | New playlist description | false   | 400        |


  Scenario Outline: Verify create Playlists with invalid Access Token
    When With request headers
      | Content-Type     | Authorization   |
      | application/json | <authorization> |
    And With request body: "src/test/resources/test-data/CreatePlaylists.json"
      | name         | description              | public |
      | New playlist | New playlist description | false  |
    When User makes a POST request to endpoint: "users/{user_id}/playlists"
    Then Response status code should be: <statusCode>
    And Response body should contains error fields
      | error.status | error.message   |
      | <statusCode> | <error.message> |
    Examples:
      | authorization               | statusCode | error.message                              |
      | Bearer invalid_access_token | 401        | Invalid access token                       |
      | invalid_access_token        | 400        | Only valid bearer authentication supported |
      | {expired_access_token}      | 401        | The access token expired                   |


  Scenario: Verify Test
    When With request headers
      | Content-Type     | Authorization  | Test |
      | application/json | {access_token} | Data |