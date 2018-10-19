SnaFoo Application
=====================

### Running the application
To run the application, run the `main` method from the `Application` class. Once running, navigate in a browser to 
`http://localhost:8080/`. From there, you can run through the application as a normal user. The `application.properties`
file contains information to connect to a database. Un-comment the database in order to connect to it. The database
includes all required snacks, plus 4 snacks, pre-loaded.

### Project Structure
* Controllers
* Exceptions
* Repository
* Resources
* Cookies

#### Controllers
I tried to split out the controllers by concern as much as possible. A couple of things to point out:
* `SortByVote` isn't a controller, as much as it is just a helper class. An argument 
   could be made for putting this in its own `helpers` package.
* `SnacksController` contains methods that should never be seen by the user. They are all back end methods.
* `SnacksPageController` contains the methods that tie the backend methods with the front-end, as well as 
   their helper methods.
* `SnacksJsonController` is a `RestController` that returns a JSON list of all snacks. Probably not necessary 
   for it to be JSON, in its own class, but each list parses this "master" list.
    * The `Snacks` class contains fields that are used to parse the main list such as `isSuggested` and `isOptional`
* `ErrorController` is a `ControllerAdvice` class that captures specific exceptions and funnels them to a specific
   view, rather than a generic error page.
   
#### Exceptions
The only custom exception is for when the "Votes" button is clicked when more then 3 are checked.

When this exception is raised, it's caught by the `ErrorController` and sent to their respective views.

The reason why there is no custom exception for when nothing is checked is because this ends up as a 
`missing parameter` error, which is caught by `MissingServletRequestParameterException`. This is also handled 
in the `ErrorController` class

#### Repositories
`SnacksRepository` is the only repository for this project because there is only one table that houses all snacks.
I only have two methods in it: `findAll()` and `findSnackById()`. I named `findSnackById()` uniquely to avoid clashing with the
already existing `findById()`, which returns `Optional` instead of `List`.

#### Resources
I'm using Bootstrap for styling and Thymeleaf for my view construction. `snacks.html` is essentially the home page. 
Each `@RequestMapping` method in `SnacksPageController` is set up to return values that Thymeleaf them uses to 
create and populate HTML elements appropriately.

#### Cookies
Cookies are set up in the two places requested by the requirements: `addVote()` and `addSuggestion()`
* The cookie created by the `addVote()` method is incremented up by `1` for each vote cast. Since `addVote()` doesn't
return a view, however, the cookie is called again by `snacksList()` so its value can be parsed and returned to the `snacks`
view. The `snacksList()` method also contains a block of code that will let Thymeleaf know whether to enable or disable
the "Vote" button on that page based on the value in the cookie.
* The cookie created by `addSuggestion()` is incremented up by `1` when a suggestion is made. Similar to the `addVote()`
cookie, it is called again by `snackSuggestion()` so its value can be parsed and returned to the view. Also similar,
the `snackSuggestion()` method contains code to let Thymeleaf know whether or not to disable the Submit button.

Cookies are an ok solution for preventing the user to continue interacting with the UI after a designated number of
interactions (in this case, adding suggestions and voting for them), but they are easily cleared, spoofed, or manipulated
by the user. Cookies should never be solely responsible for this, especially when it involves data entry.

#### Concessions
###### What did I not account for? 

If a new snack is ever _added_ to the remote API, I don't believe it would play nicely with the application as it's written.
The ID for the new snack would be `1007`, but the `addApi()` method will likely see this, try to add it to the repository,
and find that there's already a suggested snack with an ID of `1007`. In order to get around this, there could be some
code in the `addApi()` method to:
 1. Check for this 
 2. Copy the old `1007` record
 3. Replace it with the new record from
the API
 4. Add back the old `1007` record with whatever the next unused ID would be

The end result would be the old `1007` record ID becomes, for example, `1010` and the new record becomes `1007`.