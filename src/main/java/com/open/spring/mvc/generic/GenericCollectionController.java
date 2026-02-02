package com.open.spring.mvc.generic;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.open.spring.mvc.identity.User;
import com.open.spring.mvc.identity.UserRepository;

@RestController
@RequestMapping("/api/collections")
public class GenericCollectionController {

    @Autowired
    private UserCollectionItemRepository collectionRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/{type}")
    public ResponseEntity<UserCollectionItem> addItem(
            @PathVariable CollectionItemType type,
            @RequestBody UserCollectionItem item,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails != null) {
            String email = userDetails.getUsername();
            Optional<User> owner = userRepository.findByEmail(email);

            if (owner.isPresent()) {
                item.setOwner(owner.get());
            }
        }

        item.setType(type);
        UserCollectionItem saved = collectionRepository.save(item);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{param}")
    public ResponseEntity<?> listItems(@PathVariable String param) {
        // Try to parse as Long (id)
        try {
            Long id = Long.parseLong(param);
            return collectionRepository.findById(id)
                    .map(item -> new ResponseEntity<Object>(item, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (NumberFormatException e) {
            // Not a number, try as CollectionItemType
            try {
                CollectionItemType type = CollectionItemType.valueOf(param.toUpperCase());
                return new ResponseEntity<>(collectionRepository.findByType(type), HttpStatus.OK);
            } catch (IllegalArgumentException ex) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
    }

    @GetMapping("/{type}/{ownerId}")
    public ResponseEntity<List<UserCollectionItem>> listItemsByOwner(
            @PathVariable CollectionItemType type,
            @PathVariable Long ownerId) {
        return new ResponseEntity<>(collectionRepository.findByOwnerIdAndType(ownerId, type), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserCollectionItem> updateItem(
            @PathVariable Long id,
            @RequestBody UserCollectionItem updatedItem,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<UserCollectionItem> existing = collectionRepository.findById(id);
        if (existing.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        UserCollectionItem item = existing.get();
        String email = userDetails.getUsername();
        if (item.getOwner() != null && !item.getOwner().getEmail().equals(email)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Update fields
        item.setType(updatedItem.getType());
        item.setName(updatedItem.getName());
        item.setAttributes(updatedItem.getAttributes());

        UserCollectionItem saved = collectionRepository.save(item);
        return new ResponseEntity<>(saved, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<UserCollectionItem> item = collectionRepository.findById(id);
        if (item.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String email = userDetails.getUsername();
        if (item.get().getOwner() != null && !item.get().getOwner().getEmail().equals(email)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        collectionRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
