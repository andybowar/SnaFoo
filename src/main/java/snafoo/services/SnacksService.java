package snafoo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import snafoo.utilities.Snacks;
import snafoo.utilities.*;
import snafoo.repositories.SnacksRepository;

@Service
public class SnacksService {

    private final String url = "https://api-snacks-staging.nerderylabs.com/v1/snacks/?ApiKey=cda2e459-799b-4583-b697-f041ccb5000c";

    @Autowired
    private SnacksRepository snacksRepository;

    @Autowired
    private RestTemplate restTemplate;

    private List<Snacks> allSuggestedSnacks() {
        return snacksRepository.findAll();
    }

    public void addApi() throws RestClientException {
        Snacks[] snacks = new Snacks[0];

        try {
            snacks = restTemplate.getForObject(url, Snacks[].class);
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
    }

    public Long saveVote(Long numVotesAdded, int[] ids) {
        
        for (int id : ids) {
            Snacks snack = snacksRepository.findSnackById(id).get(0);

            if (snack.isOptional()) {
                int numVotes = snacksRepository.findSnackById(id).get(0).getNumVotes();
                snack.setNumVotes(numVotes + 1);
                numVotesAdded++;
            }
            snacksRepository.save(snack);
        }
        return numVotesAdded;
    }

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

    public List<Snacks> requiredSnacks() {
        addApi();
        List<Snacks> allSnacks = allSuggestedSnacks();
        List<Snacks> requiredSnacks = new ArrayList<>();

        for (Snacks snack : allSnacks) {
            if (!snack.isOptional()) {
                requiredSnacks.add(snack);
            }
        }

        return requiredSnacks;
    }

    public List<Snacks> optionalSnacks() {
        List<Snacks> allSnacks = allSuggestedSnacks();
        List<Snacks> optionalSnacks = new ArrayList<>();

        for (Snacks snack : allSnacks) {
            if(snack.isOptional()) {
                optionalSnacks.add(snack);
            }
        }

        return optionalSnacks;
    }

    public List<Snacks> suggestedSnacks() {
        List<Snacks> allSnacks = allSuggestedSnacks();
        List<Snacks> suggestedSnacks = new ArrayList<>();

        for (Snacks snack : allSnacks) {
            if (snack.isSuggested()) {
                suggestedSnacks.add(snack);
            }
        }

        return suggestedSnacks;
    }

    // Make this a POST call
    public void saveSnack(Snacks snacks) {
        snacks.setOptional(true);
        snacksRepository.save(snacks);
    }

    public void saveSuggestion(int id) {
        Snacks snack;

        snack = snacksRepository.findSnackById(id).get(0);
        snack.setSuggested(true);
        snacksRepository.save(snack);
    }
}
