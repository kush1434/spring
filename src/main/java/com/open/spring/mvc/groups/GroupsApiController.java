package com.open.spring.mvc.groups;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.open.spring.mvc.person.PersonJpaRepository;

@RestController
@RequestMapping("/api/groups")
public class GroupsApiController {

    @Autowired
    private GroupsJpaRepository groupsRepository;

    @Autowired
    private PersonJpaRepository personRepository;

    /**
     * Get all groups with their members.
     * Returns JSON structure:
     * [
     *   {
     *     "id": 1,
     *     "name": "apples",
     *     "period": "2",
     *     "members": [
     *       { "id": 4, "uid": "jm1021", "name": "John Mortensen", "email": "jmort1021@gmail.com" },
     *       ...
     *     ]
     *   },
     *   ...
     * ]
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getAllGroups() {
        try {
            List<Groups> groups = groupsRepository.findAll();
            List<Map<String, Object>> result = new ArrayList<>();

            for (Groups group : groups) {
                Map<String, Object> groupMap = new LinkedHashMap<>();
                groupMap.put("id", group.getId());
                groupMap.put("name", group.getName());
                groupMap.put("period", group.getPeriod());

                // Build members list by querying the join table directly via raw SQL
                List<Map<String, Object>> membersList = new ArrayList<>();
                List<Object[]> memberRows = groupsRepository.findGroupMembersRaw(group.getId());

                for (Object[] row : memberRows) {
                    Map<String, Object> member = new LinkedHashMap<>();
                    member.put("id", ((Number) row[0]).longValue());
                    member.put("uid", (String) row[1]);
                    member.put("name", (String) row[2]);
                    member.put("email", (String) row[3]);
                    membersList.add(member);
                }

                groupMap.put("members", membersList);
                result.add(groupMap);
            }

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
