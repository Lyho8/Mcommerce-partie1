package com.ecommerce.microcommerce.web.controller;

import java.net.URI;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.exceptions.ProduitGratuitException;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(description = "API pour les opérations CRUD sur les produits.")
@RestController
public class ProductController {

	@Autowired
	private ProductDao productDao;

	// Récupérer la liste des produits
	@RequestMapping(value = "/Produits", method = RequestMethod.GET)
	public MappingJacksonValue listeProduits() {
		Iterable<Product> produits = productDao.findAll();

		SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");

		FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);

		MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);

		produitsFiltres.setFilters(listDeNosFiltres);

		return produitsFiltres;
	}

	// Récupérer un produit par son Id
	@ApiOperation(value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock!")
	@GetMapping(value = "/Produits/{id}")
	public Product afficherUnProduit(@PathVariable int id) {
		Product produit = productDao.findById(id);

		if (produit == null)
			throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est introuvable.");

		return produit;
	}

	// Ajouter un produit
	@PostMapping(value = "/Produits")
	public ResponseEntity<Void> ajouterProduit(@Valid @RequestBody Product product) {
		// On contrôle le prix avant de tenter de sauvegarder
		// Si le prix est négatif, c'est le contrôle sur le champ "prix" qui lèvera une exception
		if(product.getPrix() == 0) {
			throw new ProduitGratuitException("Le prix de vente doit strictement être supérieur à 0 !");
		}
		
		Product productAdded = productDao.save(product);

		if (productAdded == null) {
			return ResponseEntity.noContent().build();
		}

		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(productAdded.getId()).toUri();

		return ResponseEntity.created(location).build();
	}

	@DeleteMapping(value = "/Produits/{id}")
	public void supprimerProduit(@PathVariable int id) {
		productDao.delete(id);
	}

	@PutMapping(value = "/Produits")
	public void updateProduit(@RequestBody Product product) {
		productDao.save(product);
	}

	// Pour les tests
	@GetMapping(value = "test/produits/{prix}")
	public List<Product> testDeRequetes(@PathVariable int prix) {
		return productDao.chercherUnProduitCher(400);
	}

	// Retourne la marge de l'ensemble des produits enregistrés
	@GetMapping(value = "/AdminProduits", produces = "application/json")
	public ArrayNode calculerMargeProduit() {
		ObjectMapper mapper = new ObjectMapper();

		ArrayNode result = mapper.createArrayNode();

		List<Product> products = productDao.findAll();

		ObjectNode on = null;
		for (Product p : products) {
			on = mapper.createObjectNode();
			// Le prix d'achat étant un int primitif, il est forcément au moins initialisé à
			// 0 par défaut.
			// Pas besoin de contrôle.
			on.put(p.toString(), p.getPrix() - p.getPrixAchat());

			result.add(on);
		}

		return result;
	}

	// Méthode alternative à "/Produits" qui renvoie les produits triés par ordre
	// alphabétique
	@GetMapping(value = "/ProduitsAlpha")
	public List<Product> trierProduitsParOrdreAlphabetique() {
		return productDao.findAllByOrderByNomAsc();
	}

}
