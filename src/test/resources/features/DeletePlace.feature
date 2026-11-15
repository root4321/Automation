@delete @Regression @apiRegression
Feature: Delete a place
  @positive @Sanity
  Scenario Outline: Delete an existing place
    Given an existing place with <latitude>, <longitude>, <accuracy>, "<name>", "<phone>", "<address>", and "<types>"
    When the place is deleted
    Then the place is deleted successfully

    Examples:
      | latitude   | longitude | accuracy | name            | phone              | address                   | types          |
      | -38.383494 | 33.427362 | 50       | Frontline house | (+91) 983 893 3937 | 29, side layout, cohen 09 | shoe park;shop |

  @negative
  Scenario: Reject deletion of an unknown place
    Given the place identifier is "does-not-exist"
    When the place is deleted
    Then the place operation is rejected with a client error

  @negative @edge
  Scenario: Reject deletion with an empty place identifier
    Given the place identifier is ""
    When the place is deleted
    Then the place operation is rejected with a client error

  @negative @edge
  Scenario: Reject deletion without a place identifier
    Given the place identifier is omitted
    When the place is deleted
    Then the place operation is rejected with a client error

  @negative @edge
  Scenario Outline: A place cannot be deleted twice
    Given an existing place with <latitude>, <longitude>, <accuracy>, "<name>", "<phone>", "<address>", and "<types>"
    And the existing place has been deleted
    When the place is deleted
    Then the place operation is rejected with a client error

    Examples:
      | latitude   | longitude | accuracy | name            | phone              | address                   | types          |
      | -38.383494 | 33.427362 | 50       | Frontline house | (+91) 983 893 3937 | 29, side layout, cohen 09 | shoe park;shop |
