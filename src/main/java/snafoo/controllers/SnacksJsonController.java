package snafoo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import snafoo.Snacks;
import snafoo.repositories.SnacksRepository;

@RestController
public class SnacksJsonController {
    @Autowired
    private SnacksRepository snacksRepository;

    @RequestMapping("json-all-snacks")
    public List<Snacks> allSuggestedSnacks() {
        return snacksRepository.findAll();
    }
}
