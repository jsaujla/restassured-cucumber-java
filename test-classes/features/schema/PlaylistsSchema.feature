Feature: Playlists Schema

  Background:
    Given An authorized user is available

  Scenario: Verify create Playlists schema
    When With request headers
      | Content-Type     | Authorization  |
      | application/json | {access_token} |
    And With request body: "src/test/resources/test-data/CreatePlaylists.json"
      | name         | description              | public |
      | New Playlist | New playlist description | false  |
    When User makes a POST request to endpoint: "users/{{user_id}}/playlists"
    Then Response status code should be: 201
    And Response body should match with schema: "src/test/resources/schema/PlaylistSchema.json"


  Scenario Outline: Verify Playlists Error schema
    When With request headers
      | Content-Type     | Authorization  |
      | application/json | {access_token} |
    And With request body: "src/test/resources/test-data/CreatePlaylists.json"
      | name   | description   | public   |
      | <name> | <description> | <public> |
    When User makes a POST request to endpoint: "users/{{user_id}}/playlists"
    Then Response status code should be: <statusCode>
    And Response body should match with schema: "src/test/resources/schema/ErrorSchema.json"
    Examples:
      | name | description              | public  | statusCode |
      |      | New playlist description | false   | 400        |
