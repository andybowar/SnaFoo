package snafoo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import snafoo.utilities.Snacks;
import snafoo.exceptions.TooManyVotesException;
import snafoo.services.SnacksService;

@Controller
public class SnacksPageController {

    @Autowired
    private SnacksService snacksService;

    @GetMapping("/")
    public String snacksList(Model model, HttpServletRequest request) {
        snacksService.addApi();

        int intNumVotes = 0;

        model.addAttribute("requiredSnacks", snacksService.requiredSnacks());
        model.addAttribute("suggestedSnacks", snacksService.suggestedSnacks());

        /* Get the numVotesAdded cookie, which was created in the addVote() method
         We call the cookie and manipulate it here because this method returns the
         view to which we need to send the value. */
        Cookie numVotesAdded = WebUtils.getCookie(request, "numVotesAdded");

        /* This block controls whether the `Submit` button is disabled or not.
           Every time `/` is called, it will check the cookie value and send back
           `true` when it's reached 3. */
        if(!(numVotesAdded == null)) {
            intNumVotes = Integer.valueOf(numVotesAdded.getValue());
            if(intNumVotes >= 3) {
                model.addAttribute("disableSubmitVotesButton", true);
            }
        }

        model.addAttribute("intNumVotes", intNumVotes);

        return "snacks";
    }

    @RequestMapping("add-snack-success")
    public String addSnack (@ModelAttribute("snacks") @Validated Snacks snacks) {

        snacksService.saveSnack(snacks);

        return "redirect:/snack-suggestions";
    }

    @GetMapping("/snack-suggestions")
    public String snackSuggestions(Model model, HttpServletRequest request) {
        int intNumAdded = 0;

        snacksService.addApi();
        model.addAttribute("optionalSnacks", snacksService.optionalSnacks());
        model.addAttribute("newSnack", new Snacks());

        Cookie numAdded = WebUtils.getCookie(request, "numAdded");

        // This block controls whether the `Submit` button is disabled or not
        if(!(numAdded == null)) {
            intNumAdded = Integer.valueOf(numAdded.getValue());
            if(intNumAdded >= 1) {
                model.addAttribute("disableSubmitSnackButton", true);
            }
        }
        model.addAttribute("intNumAdded", intNumAdded);

        return "suggestions";
    }

    @GetMapping("/shopping-list")
    public String shoppingList(Model model) {
        List<Snacks> shoppingList = snacksService.shoppingList();

        model.addAttribute("shoppingList", shoppingList);

        return "shopping-list";
    }

    @RequestMapping("add-suggestion")
    public String addSuggestion(@CookieValue(value = "numAdded", defaultValue = "0") Long numAdded, HttpServletResponse response, @RequestParam int id) {

        snacksService.saveSuggestion(id);

        numAdded++;

        Cookie cookie = new Cookie("numAdded", numAdded.toString());
        response.addCookie(cookie);

        return "redirect:/snack-suggestions";
    }

    @RequestMapping("add-vote")
    public String addVote(@CookieValue(value = "numVotesAdded", defaultValue = "0") Long numVotesAdded, HttpServletResponse response, @RequestParam int[] ids) throws TooManyVotesException {
        // Prevents user from checking and voting for more than 3 options
        if(ids.length > 3) {
            throw new TooManyVotesException("You may only vote for 3 snacks or fewer.");
        }

        // this part does not work.
        numVotesAdded = snacksService.saveVote(numVotesAdded, ids);

        Cookie cookie = new Cookie("numVotesAdded", numVotesAdded.toString());
        response.addCookie(cookie);

        return "redirect:/";
    }
}
