@ui @login @Regression
Feature: Shop login
   
  @uiSanity @healthcheck @uiRegression 
  Scenario: Sign in with a registered account
    Given the shop login page is open
    When I sign in with the configured shop credentials
    Then the shop dashboard is displayed
