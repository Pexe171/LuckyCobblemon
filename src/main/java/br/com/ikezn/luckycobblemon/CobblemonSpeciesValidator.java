package br.com.ikezn.luckycobblemon;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.util.Identifier;

final class CobblemonSpeciesValidator {
    private static final String REGISTRY_CLASS = "com.cobblemon.mod.common.api.pokemon.PokemonSpecies";

    private CobblemonSpeciesValidator() {
    }

    static void requireKnown(String species) {
        try {
            Class<?> registry = Class.forName(REGISTRY_CLASS);
            Object found;
            if (species.contains(":")) {
                Method method = registry.getMethod("getByIdentifier", Identifier.class);
                found = method.invoke(null, Identifier.of(species));
            } else {
                Method method = registry.getMethod("getByName", String.class);
                found = method.invoke(null, species);
            }
            if (found == null) {
                throw new IllegalArgumentException("unknown Cobblemon species: " + species);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException error) {
            throw new IllegalStateException("Cobblemon species registry is unavailable", error);
        } catch (InvocationTargetException error) {
            throw new IllegalArgumentException("could not validate Cobblemon species: " + species, error.getCause());
        }
    }
}
