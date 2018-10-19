package snafoo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import snafoo.Snacks;
import snafoo.exceptions.TooManyVotesException;
import snafoo.repositories.SnacksRepository;

@Controller
public class SnacksController {

    @Autowired
    private SnacksRepository snacksRepository;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("add-api")
    public String addApi() throws RestClientException {
        Snacks[] snacks = new Snacks[0];

        try {
            snacks = restTemplate.getForObject("https://api-snacks-staging.nerderylabs.com/v1/snacks/?ApiKey=cda2e459-799b-4583-b697-f041ccb5000c", Snacks[].class);
        } catch (RestClientException e) {
            System.out.println("Could not get required snacks from remote API: " + e);
        }

        if (snacks != null) {
            for (Snacks snack : snacks) {
                List<Snacks> snacksRepo = snacksRepository.findAll();
                if (snacksRepo.contains(snack)) {
                    break;
                } else {
                    snacksRepository.save(snack);
                }
            }
        }

        return "redirect:/";
    }

    @RequestMapping("add-snack-success")
    public String addSnack (@ModelAttribute("snacks") @Validated Snacks snacks) {

        snacks.setOptional(true);
        snacksRepository.save(snacks);

        return "redirect:/snack-suggestions";
    }

    @RequestMapping("add-suggestion")
    public String addSuggestion(@CookieValue(value = "numAdded", defaultValue = "0") Long numAdded, HttpServletResponse response, @RequestParam int id) {

        Snacks snack = snacksRepository.findSnackById(id).get(0);
        snack.setSuggested(true);
        snacksRepository.save(snack);

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

        for (int id : ids) {
            Snacks snack = snacksRepository.findSnackById(id).get(0);

            if (snack.isOptional()) {
                int numVotes = snacksRepository.findSnackById(id).get(0).getNumVotes();
                snack.setNumVotes(numVotes + 1);
            }
            numVotesAdded++;
            snacksRepository.save(snack);
        }

        Cookie cookie = new Cookie("numVotesAdded", numVotesAdded.toString());
        response.addCookie(cookie);

        return "redirect:/";
    }

    @RequestMapping("shopping-list")
    public List<Snacks> shoppingList() {
        List<Snacks> snacks = snacksRepository.findAll();

        List<Snacks> shoppingList = new ArrayList<>();

        // First, add all required snacks to shopping list
        for (Snacks snack : snacks) {
            if (!snack.isOptional()) {
                shoppingList.add(snack);
            }
        }

        // Then, remove all required items and non-suggested items
        // from repo list
        for (int i = 0; i < snacks.size();) {
            if (!snacks.get(i).isOptional() || !snacks.get(i).isSuggested()) {
                snacks.remove(snacks.get(i));
            } else {
                i++;
            }
        }

        // Finalize the number of required items
        final int requiredItemsShoppingListSize = shoppingList.size();

        // Sort the remaining snacks by number of votes so that the snacks
        // are added to the shopping list in that order
        snacks.sort(new SortByVote());

        // First, determine if we should just add all remaining snacks to
        // the list. If there are more than 10 total snacks, only add enough
        // snacks from the optional snacks for the total number to reach 10.
        // Due to the sorting done above, the optional snacks will be added
        // in order of number of votes.
        if(snacks.size() < (10 - requiredItemsShoppingListSize)) {
            shoppingList.addAll(snacks);
        } else {
            for(int i = 0; i < (10 - requiredItemsShoppingListSize); i++) {
                shoppingList.add(snacks.get(i));
            }
        }

        return shoppingList;
    }
}
