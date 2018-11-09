package snafoo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import snafoo.repositories.VotesRepository;
import snafoo.utilities.Snacks;
import snafoo.utilities.*;
import snafoo.repositories.SnacksRepository;

@Service
public class SnacksService {

    private final String url = "https://api-snacks-staging.nerderylabs.com/v1/snacks/?ApiKey=cda2e459-799b-4583-b697-f041ccb5000c";

    @Autowired
    private SnacksRepository snacksRepository;

    @Autowired
    private VotesRepository votesRepository;

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

        /*
         * 1. Iterate through all snacks in the API
         * 2. Find all snacks in the database
         * 3. If there are none in the database, skip down and save the first snack
         * 4. If there is at least one snack in the database, check each snack from
         *    the API (by ID) to see if it already exists in the database
         * 5. If it doesn't, add it
         * 6. By the end of this loop, all snacks from the API should be synced to
         *    the database without duplicates.
         */
        if (snacks != null) {
            for (Snacks snack : snacks) {
                int i = 0;
                List<Snacks> snacksRepo = snacksRepository.findAll();
                if (snacksRepo.size() > 0) {
                    if (snacksRepo.get(i).getId().equals(snack.getId())) {
                        break;
                    } else {
                        snacksRepository.save(snack);
                    }
                } else {
                    snacksRepository.save(snack);
                }

            }
        }
    }

    public void postToApi() {
        List<Snacks> snacks = snacksRepository.findAll();

        for (Snacks snack : snacks) {
            Map<String, String> map = new HashMap<>();
            map.put("name", snack.getName());
            map.put("location", snack.getPurchaseLocations());
            try {
                if (snack.isOptional()) {
                    restTemplate.postForObject(url, map, Snacks.class);
                }
            } catch (HttpClientErrorException expected) {
                // Any optional snacks already in the API will conflict,
                // producing a 409 error.
            }
        }

    }

    public Long saveVote(Long numVotesAdded, int[] ids) {

        // Save the vote as a new row in the Votes table
        for (int id : ids) {
            Snacks snack = snacksRepository.findSnackById(id).get(0);

            int snackId = snack.getId();
            Votes votes = new Votes();
            votes.setSnackId(snackId);
            votesRepository.save(votes);

            numVotesAdded++;
        }

        List<Snacks> snacks = snacksRepository.findAll();
        List<Votes> votes = votesRepository.findAll();

        // Put all snack IDs from the Votes table into an array in order
        // to count number of votes by snack ID
        List<String> votedSnackIds = new ArrayList<>();
        for (Votes vote : votes) {
            String snackId = String.valueOf(vote.getSnackId());
            votedSnackIds.add(snackId);
        }

        // Count number of votes by snack ID, set the number in the
        // snack object and save
        for (Snacks snack: snacks) {
            int numVotes = Collections.frequency(votedSnackIds, String.valueOf(snack.getId()));
            snack.setNumVotes(numVotes);
            snacksRepository.save(snack);
        }

        // Return this for the cookie
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
        //addApi();
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

    public void saveSnack(Snacks snacks) {
        snacks.setOptional(true);

        // Post snack to API so it gets an ID
        Map<String, String> map = new HashMap<>();
        map.put("name", snacks.getName());
        map.put("location", snacks.getPurchaseLocations());
        try {
            if (snacks.isOptional()) {
                restTemplate.postForObject(url, map, Snacks.class);
            }
        } catch (HttpClientErrorException expected) {

        }

        // Get snack back from API to get its ID so we can save it to database
        Snacks[] snacksInApi = restTemplate.getForObject(url, Snacks[].class);
        for (Snacks snackInApi : snacksInApi) {
            if(snackInApi.getName().equals(snacks.getName())) {
                snacks.setId(snackInApi.getId());
            }
        }

        snacksRepository.save(snacks);
    }

    public void saveSuggestion(int id) {
        Snacks snack;

        snack = snacksRepository.findSnackById(id).get(0);
        snack.setSuggested(true);
        snacksRepository.save(snack);
    }
}
