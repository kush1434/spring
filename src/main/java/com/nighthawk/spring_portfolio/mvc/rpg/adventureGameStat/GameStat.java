package com.nighthawk.spring_portfolio.mvc.rpg.adventureGameStat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.rpg.gamifyGame.Game;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class GameStat {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // user uid
    @OneToOne
    @JoinColumn(name = "person_uid", nullable = false)
    private Person uid;

    // game uid
    @OneToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game gid;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> stats = new HashMap<>();


    public GameStat (Person uid, Game gid, Map<String, Object> stats) {
        this.uid = uid;
        this.gid = gid;
        this.stats = stats;
    }    

    public static GameStat createGameStats(Person uid, Game gid, Map<String, Object> stats) {
        GameStat gamestats = new GameStat();
        gamestats.setUid(uid);
        gamestats.setGid(gid);
        gamestats.setStats(stats);

        return gamestats;
    }

    public static GameStat[] init() {
        ArrayList<GameStat> gamestats = new ArrayList<>();
        
        

        return gamestats.toArray(new GameStat[0]);
    }
}

