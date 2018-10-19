package snafoo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import snafoo.Snacks;

@Controller
public class SnacksPageController {

    @Autowired
    private SnacksController snacksController;

    @Autowired
    private SnacksJsonController snacksJsonController;

    private List<Snacks> requiredSnacks() {
        snacksController.addApi();
        List<Snacks> allSnacks = snacksJsonController.allSuggestedSnacks();
        List<Snacks> requiredSnacks = new ArrayList<>();

        for (Snacks snack : allSnacks) {
            if (!snack.isOptional()) {
                requiredSnacks.add(snack);
            }
        }

        return requiredSnacks;
    }

    private List<Snacks> optionalSnacks() {
        List<Snacks> allSnacks = snacksJsonController.allSuggestedSnacks();
        List<Snacks> optionalSnacks = new ArrayList<>();

        for (Snacks snack : allSnacks) {
            if(snack.isOptional()) {
                optionalSnacks.add(snack);
            }
        }

        return optionalSnacks;
    }

    private List<Snacks> suggestedSnacks() {
        List<Snacks> allSnacks = snacksJsonController.allSuggestedSnacks();
        List<Snacks> suggestedSnacks = new ArrayList<>();

        for (Snacks snack : allSnacks) {
            if (snack.isSuggested()) {
                suggestedSnacks.add(snack);
            }
        }

        return suggestedSnacks;
    }

    @GetMapping("/")
    public String snacksList(Model model, HttpServletRequest request) {
        int intNumVotes = 0;

        model.addAttribute("requiredSnacks", requiredSnacks());
        model.addAttribute("suggestedSnacks", suggestedSnacks());

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

    @GetMapping("/snack-suggestions")
    public String snackSuggestions(Model model, HttpServletRequest request) {
        int intNumAdded = 0;

        snacksController.addApi();
        model.addAttribute("optionalSnacks", optionalSnacks());
        model.addAttribute("newSnack", new Snacks());

        Cookie numAdded = WebUtils.getCookie(request, "numAdded");

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
        List<Snacks> shoppingList = snacksController.shoppingList();

        model.addAttribute("shoppingList", shoppingList);

        return "shopping-list";
    }
}
