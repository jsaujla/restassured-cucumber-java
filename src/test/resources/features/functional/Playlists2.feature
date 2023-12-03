@regression
Feature: Playlists

  Background:
    Given An authorized user is available

  Scenario: User should be able to create a playlist
    When With request headers
      | Content-Type     | Authorization  |
      | application/json | {access_token} |
    And With request body: ""
      | name         | description              | public |
      | New Playlist | New playlist description | false  |
    When User makes a POST request to endpoint: "users/user_id/playlists"
    Then Response status code should be: 201
    And Response body should contains fields
      | id       | name         | description              | public |
      | NOT_NULL | New Playlist | New playlist description | false  |


  Scenario: User should be able to get a playlist
    When With request headers
      | Content-Type     | Authorization  |
      | application/json | {access_token} |
    #When User makes a GET request to endpoint: "playlists/4rFswJtKb40gOhjimJxrWZ"
    When User makes a GET request to endpoint: "playlists/{idFromPostRequest}"
    Then Response status code should be: 200
    And Response body should contains fields
      | id                       | name             | description                  | public |
      | {4rFswJtKb40gOhjimJxrWZ} | Updated Playlist | Updated playlist description | false  |

#  Scenario: User should be able to change/update a playlist details
#    When With request headers
#      | Content-Type     | Authorization  |
#      | application/json | {access_token} |
#    And With request body: ""
#      | name             | description                  | public |
#      | Updated Playlist | Updated playlist description | false  |
#    When User makes a PUT request to endpoint: "playlists/4rFswJtKb40gOhjimJxrWZ"
#    Then Response status code should be: 200
#
#    When With request headers
#      | Content-Type     | Authorization  |
#      | application/json | {access_token} |
#    When User makes a GET request to endpoint: "playlists/4rFswJtKb40gOhjimJxrWZ"
#    Then Response status code should be: 200
#    And Response body should contains fields
#      | id                     | name             | description                  | public |
#      | 4rFswJtKb40gOhjimJxrWZ | Updated Playlist | Updated playlist description | false  |
