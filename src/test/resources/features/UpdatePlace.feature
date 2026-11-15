@put @Regression
Feature: Update a place
  @positive @Sanity
  Scenario Outline: Update the address of an existing place
    Given an existing place with <latitude>, <longitude>, <accuracy>, "<name>", "<phone>", "<address>", and "<types>"
    And the update address is "70 winter walk, USA"
    When the place address is updated
    Then the address is updated successfully

    Examples:
      | latitude   | longitude | accuracy | name            | phone              | address                   | types          |
      | -38.383494 | 33.427362 | 50       | Frontline house | (+91) 983 893 3937 | 29, side layout, cohen 09 | shoe park;shop |

  @negative
  Scenario: Reject an update for an unknown place
    Given the place identifier is "does-not-exist"
    When the place address is updated
    Then the place operation is rejected with a client error

  @negative @edge
  Scenario: Reject an update with an empty place identifier
    Given the place identifier is ""
    When the place address is updated
    Then the place operation is rejected with a client error

  @negative @edge
  Scenario: Reject an update without a place identifier
    Given the place identifier is omitted
    When the place address is updated
    Then the place operation is rejected with a client error

  @negative @edge
  Scenario Outline: Reject an update without an address
    Given an existing place with <latitude>, <longitude>, <accuracy>, "<name>", "<phone>", "<address>", and "<types>"
    And the update address is omitted
    When the place address is updated
    Then the place operation is rejected with a client error

    Examples:
      | latitude   | longitude | accuracy | name            | phone              | address                   | types          |
      | -38.383494 | 33.427362 | 50       | Frontline house | (+91) 983 893 3937 | 29, side layout, cohen 09 | shoe park;shop |

  @negative @edge
  Scenario Outline: Reject an update with an empty address
    Given an existing place with <latitude>, <longitude>, <accuracy>, "<name>", "<phone>", "<address>", and "<types>"
    And the update address is ""
    When the place address is updated
    Then the place operation is rejected with a client error

    Examples:
      | latitude   | longitude | accuracy | name            | phone              | address                   | types          |
      | -38.383494 | 33.427362 | 50       | Frontline house | (+91) 983 893 3937 | 29, side layout, cohen 09 | shoe park;shop |

  @negative @edge
  Scenario Outline: A deleted place cannot be updated
    Given an existing place with <latitude>, <longitude>, <accuracy>, "<name>", "<phone>", "<address>", and "<types>"
    And the existing place has been deleted
    When the place address is updated
    Then the place operation is rejected with a client error

    Examples:
      | latitude   | longitude | accuracy | name            | phone              | address                   | types          |
      | -38.383494 | 33.427362 | 50       | Frontline house | (+91) 983 893 3937 | 29, side layout, cohen 09 | shoe park;shop |
