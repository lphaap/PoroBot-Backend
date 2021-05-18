package app;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import database.DBHandler;
import database.Player;


@RestController
@RequestMapping("api/")
public class SpringController {

	@CrossOrigin
	@GetMapping("games/{id}")
	public List<Player> getNumbers(@PathVariable Long id){
		System.out.println(id);
		DBHandler db = new DBHandler();
		return db.findPlayers(id);
	}
}
