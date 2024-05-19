package com.gvtech.repository.recipe;


import com.gvtech.entity.recipe.*;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
@Startup
public class RecipeRepository {

    @PersistenceContext
    EntityManager em;

    @SuppressWarnings("unchecked")
    @Transactional
    public RecipeEntity findBy(final String language, final Long recipeId) {
        final String sql = "SELECT r.recipe_id AS r_recipe_id, r.image AS r_image, r.ready_in_minutes AS r_ready_in_minutes, r.servings AS r_servings, r.url AS r_url," +
                " rde.summary AS rde_summary, rde.title as rde_title, c.cuisine_id AS c_cuisine_id, c.name as c_name, d.diet_id AS d_diet_id, d.name as d_name, t.type_id AS t_type_id, t.name as t_name" +
                " FROM \"recipe\".recipe r JOIN \"recipe\".recipe_details rde ON r.recipe_id = rde.recipe_id JOIN \"recipe\".recipe_cuisine rc ON r.recipe_id = rc.recipe_id JOIN \"recipe\".cuisine c ON c.cuisine_id = rc.cuisine_id " +
                " JOIN \"recipe\".recipe_diet rd ON r.recipe_id = rd.recipe_id JOIN \"recipe\".diet d ON d.diet_id = rd.diet_id JOIN \"recipe\".recipe_type rt ON r.recipe_id = rt.recipe_id JOIN \"recipe\".type t ON t.type_id = rt.type_id " +
                " WHERE rde.language = :language AND c.language = :language AND d.language = :language AND t.language = :language AND r.recipe_id = :recipeId";

        final Query query = em.createNativeQuery(sql, Tuple.class);
        query.setParameter("language", language);
        query.setParameter("recipeId", recipeId);

        return map(query.getResultList());
    }

    @Transactional
    public List<RecipeEntity> findBy(final String language, final List<Long> recipeIds) {
        final String sql = "SELECT r.recipe_id AS r_recipe_id, r.image AS r_image, r.ready_in_minutes AS r_ready_in_minutes, r.servings AS r_servings, r.url AS r_url," +
                " rde.summary AS rde_summary, rde.title as rde_title, c.cuisine_id AS c_cuisine_id, c.name as c_name, d.diet_id AS d_diet_id, d.name as d_name, t.type_id AS t_type_id, t.name as t_name" +
                " FROM \"recipe\".recipe r JOIN \"recipe\".recipe_details rde ON r.recipe_id = rde.recipe_id JOIN \"recipe\".recipe_cuisine rc ON r.recipe_id = rc.recipe_id JOIN \"recipe\".cuisine c ON c.cuisine_id = rc.cuisine_id " +
                " JOIN \"recipe\".recipe_diet rd ON r.recipe_id = rd.recipe_id JOIN \"recipe\".diet d ON d.diet_id = rd.diet_id JOIN \"recipe\".recipe_type rt ON r.recipe_id = rt.recipe_id JOIN \"recipe\".type t ON t.type_id = rt.type_id " +
                " WHERE rde.language = :language AND c.language = :language AND d.language = :language AND t.language = :language AND r.recipe_id IN (:recipeIds)";

        final Query query = em.createNativeQuery(sql, Tuple.class);
        query.setParameter("language", language);
        query.setParameter("recipeIds", recipeIds);

        return mapList(query.getResultList());
    }


    @SuppressWarnings("unchecked")
    @Transactional
    public Set<Long> findBy(final String language, final List<Long> cuisines, final List<Long> diets, final List<Long> types) {
        final String sqlBase = "SELECT r.recipe_id AS r_recipe_id, r.image AS r_image, r.ready_in_minutes AS r_ready_in_minutes, r.servings AS r_servings, r.url AS r_url," +
                " rde.summary AS rde_summary, rde.title as rde_title, c.cuisine_id AS c_cuisine_id, c.name as c_name, d.diet_id AS d_diet_id, d.name as d_name, t.type_id AS t_type_id, t.name as t_name" +
                " FROM \"recipe\".recipe r JOIN \"recipe\".recipe_details rde ON r.recipe_id = rde.recipe_id JOIN \"recipe\".recipe_cuisine rc ON r.recipe_id = rc.recipe_id JOIN \"recipe\".cuisine c ON c.cuisine_id = rc.cuisine_id " +
                " JOIN \"recipe\".recipe_diet rd ON r.recipe_id = rd.recipe_id JOIN \"recipe\".diet d ON d.diet_id = rd.diet_id JOIN \"recipe\".recipe_type rt ON r.recipe_id = rt.recipe_id JOIN \"recipe\".type t ON t.type_id = rt.type_id " +
                " WHERE rde.language = :language AND c.language = :language AND d.language = :language AND t.language = :language AND c.enabled = true AND d.enabled = true AND t.enabled = true %s %s %s";

        String cuisinesWhereClause = "";
        String dietsWhereClause = "";
        String typesWhereClause = "";

        if (!cuisines.isEmpty()) {
            cuisinesWhereClause = "AND rc.cuisine_id IN (:cuisines) ";
        }
        if (!diets.isEmpty()) {
            dietsWhereClause = "AND rd.diet_id IN (:diets) ";
        }
        if (!types.isEmpty()) {
            typesWhereClause = "AND rt.type_id IN (:types) ";
        }


        final String sql = String.format(sqlBase, cuisinesWhereClause, dietsWhereClause, typesWhereClause);

        final Query query = em.createNativeQuery(sql, Tuple.class);
        query.setParameter("language", language);


        if (!cuisines.isEmpty()) {
            query.setParameter("cuisines", cuisines);
        }
        if (!diets.isEmpty()) {
            query.setParameter("diets", diets);
        }
        if (!types.isEmpty()) {
            query.setParameter("types", types);
        }

        return mapIds(query.getResultList());
    }


    private Set<Long> mapIds(final List<Tuple> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        final Set<Long> ids = new HashSet<>();
        for (Tuple row : rows) {
            ids.add((Long) row.get("r_recipe_id"));
        }

        return ids;
    }


    private List<RecipeEntity> mapList(final List<Tuple> rows) {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        final List<RecipeEntity> recipeEntities = new ArrayList<>();

        long currentId = (Long) rows.getFirst().get("r_recipe_id");
        final List<Tuple> toProcess = new ArrayList<>();
        for (Tuple row : rows) {
            if (currentId != (long) row.get("r_recipe_id")) {
                currentId = (long) row.get("r_recipe_id");
                recipeEntities.add(map(toProcess));
                toProcess.clear();
            }
            toProcess.add(row);
        }

        recipeEntities.add(map(toProcess));
        return recipeEntities;
    }

    @SneakyThrows
    private RecipeEntity map(final List<Tuple> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        boolean entityProcessed = false;
        boolean recipeDetailsProcessed = false;
        final Set<Long> cuisinesProcessed = new HashSet<>();
        final Set<Long> dietsProcessed = new HashSet<>();
        final Set<Long> typesProcessed = new HashSet<>();

        RecipeEntity recipeEntity = null;
        for (Tuple row : rows) {
            if (!entityProcessed) {
                recipeEntity = new RecipeEntity((Long) row.get("r_recipe_id"), (String) row.get("r_image"), (Integer) row.get("r_ready_in_minutes"), (Integer) row.get("r_servings"), (String) row.get("r_url"),
                        new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                entityProcessed = true;
            }

            if (!recipeDetailsProcessed) {
                recipeEntity.getDetails().add(new RecipeDetailsEntity(null, null, null, (String) row.get("rde_summary"), (String) row.get("rde_title")));
                recipeDetailsProcessed = true;
            }

            final Long cuisineId = (Long) row.get("c_cuisine_id");
            if (cuisinesProcessed.add(cuisineId)) {
                recipeEntity.getCuisines().add(new CuisineEntity(null, null, null, null, (String) row.get("c_name"), null));
            }
            final Long dietId = (Long) row.get("d_diet_id");
            if (dietsProcessed.add(dietId)) {
                recipeEntity.getDiets().add(new DietEntity(null, null, null, null, (String) row.get("d_name"), null));
            }
            final Long typeId = (Long) row.get("t_type_id");
            if (typesProcessed.add(typeId)) {
                recipeEntity.getTypes().add(new TypeEntity(null, null, null, null, (String) row.get("t_name"), null));
            }
        }

        return recipeEntity;
    }


}
