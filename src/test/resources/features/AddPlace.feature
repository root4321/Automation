@Regression
Feature: Add a place
  @positive @Sanity @apiSanity @healthcheck
  Scenario Outline: Add a place with valid details
    Given a valid add place request with <latitude>, <longitude>, <accuracy>, "<name>", "<phone>", "<address>", and "<types>"
    When the add place request is sent
    Then the place is added successfully

    Examples:
      | latitude   | longitude | accuracy | name            | phone              | address                   | types          |
      | -38.383494 | 33.427362 | 50       | Frontline house | (+91) 983 893 3937 | 29, side layout, cohen 09 | shoe park;shop |

  @edge
  Scenario Outline: Add a place at valid coordinate boundaries
    Given a valid add place request with <latitude>, <longitude>, <accuracy>, "<name>", "<phone>", "<address>", and "<types>"
    When the add place request is sent
    Then the place is added successfully

    Examples:
      | latitude | longitude | accuracy | name            | phone              | address                   | types          |
      | -90.0    | -180.0    | 50       | Frontline house | (+91) 983 893 3937 | 29, side layout, cohen 09 | shoe park;shop |
      | 90.0     | 180.0     | 50       | Frontline house | (+91) 983 893 3937 | 29, side layout, cohen 09 | shoe park;shop |

  @negative
  Scenario Outline: Reject an invalid API key
    Given a valid add place request with <latitude>, <longitude>, <accuracy>, "<name>", "<phone>", "<address>", and "<types>"
    And the API key is "invalid"
    When the add place request is sent
    Then the request is rejected with a client error

    Examples:
      | latitude   | longitude | accuracy | name            | phone              | address                   | types          |
      | -38.383494 | 33.427362 | 50       | Frontline house | (+91) 983 893 3937 | 29, side layout, cohen 09 | shoe park;shop |

  @negative
  Scenario Outline: Reject a missing API key
    Given a valid add place request with <latitude>, <longitude>, <accuracy>, "<name>", "<phone>", "<address>", and "<types>"
    And the API key is omitted
    When the add place request is sent
    Then the request is rejected with a client error

    Examples:
      | latitude   | longitude | accuracy | name            | phone              | address                   | types          |
      | -38.383494 | 33.427362 | 50       | Frontline house | (+91) 983 893 3937 | 29, side layout, cohen 09 | shoe park;shop |

  @negative
  Scenario Outline: Reject an invalid request body
    Given the request body is "<body>"
    When the add place request is sent
    Then the request is rejected with a client error

    Examples:
      | body |
      |      |
      | {    |
      | {}   |

  @negative
  Scenario Outline: Reject a request without a place name
    Given a valid add place request with <latitude>, <longitude>, <accuracy>, "<name>", "<phone>", "<address>", and "<types>"
    And the place name is omitted
    When the add place request is sent
    Then the request is rejected with a client error

    Examples:
      | latitude   | longitude | accuracy | name            | phone              | address                   | types          |
      | -38.383494 | 33.427362 | 50       | Frontline house | (+91) 983 893 3937 | 29, side layout, cohen 09 | shoe park;shop |

  @negative @edge
  Scenario Outline: Reject coordinates outside their valid range
    Given a valid add place request with <latitude>, <longitude>, <accuracy>, "<name>", "<phone>", "<address>", and "<types>"
    When the add place request is sent
    Then the request is rejected with a client error

    Examples:
      | latitude | longitude | accuracy | name            | phone              | address                   | types          |
      | -90.1    | 0.0       | 50       | Frontline house | (+91) 983 893 3937 | 29, side layout, cohen 09 | shoe park;shop |
      | 90.1     | 0.0       | 50       | Frontline house | (+91) 983 893 3937 | 29, side layout, cohen 09 | shoe park;shop |
      | 0.0      | -180.1    | 50       | Frontline house | (+91) 983 893 3937 | 29, side layout, cohen 09 | shoe park;shop |
      | 0.0      | 180.1     | 50       | Frontline house | (+91) 983 893 3937 | 29, side layout, cohen 09 | shoe park;shop |
