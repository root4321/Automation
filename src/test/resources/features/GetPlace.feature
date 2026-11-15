@get @Regression @apiRegression
Feature: Get a place
  @positive @Sanity
  Scenario Outline: Retrieve the details of an existing place
    Given an existing place with <latitude>, <longitude>, <accuracy>, "<name>", "<phone>", "<address>", and "<types>"
    When the place is retrieved
    Then the response contains the original place details

    Examples:
      | latitude   | longitude | accuracy | name            | phone              | address                   | types          |
      | -38.383494 | 33.427362 | 50       | Frontline house | (+91) 983 893 3937 | 29, side layout, cohen 09 | shoe park;shop |

  @negative
  Scenario: Reject an unknown place identifier
    Given the place identifier is "does-not-exist"
    When the place is retrieved
    Then the place operation is rejected with a client error

  @negative @edge
  Scenario: Reject an empty place identifier
    Given the place identifier is ""
    When the place is retrieved
    Then the place operation is rejected with a client error

  @negative @edge
  Scenario: Reject a missing place identifier
    Given the place identifier is omitted
    When the place is retrieved
    Then the place operation is rejected with a client error

  @negative @edge
  Scenario Outline: A deleted place cannot be retrieved
    Given an existing place with <latitude>, <longitude>, <accuracy>, "<name>", "<phone>", "<address>", and "<types>"
    And the existing place has been deleted
    When the place is retrieved
    Then the place operation is rejected with a client error

    Examples:
      | latitude   | longitude | accuracy | name            | phone              | address                   | types          |
      | -38.383494 | 33.427362 | 50       | Frontline house | (+91) 983 893 3937 | 29, side layout, cohen 09 | shoe park;shop |
