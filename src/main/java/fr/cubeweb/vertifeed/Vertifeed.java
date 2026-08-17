package fr.cubeweb.vertifeed;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class Vertifeed implements ModInitializer {
	public static final String MOD_ID = "vertifeed";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		preloadGuavaFilterClasses();
	}

	/**
	 * LabyMod + Java 25 can race Guava inner-class init on ForkJoinPool while
	 * decoding registries ({@code Sets$FilteredSet} ClassCircularityError).
	 * Defining those classes on the main thread first avoids the crash.
	 */
	private static void preloadGuavaFilterClasses() {
		try {
			Class.forName("com.google.common.collect.Sets$FilteredSet");
			Class.forName("com.google.common.collect.Sets$FilteredSortedSet");
			Class.forName("com.google.common.collect.Maps$FilteredKeyMap");
			Class.forName("com.google.common.collect.Maps$AbstractFilteredMap");
			Class.forName("com.google.common.collect.Maps$FilteredEntryMap");
			Class.forName("com.google.common.collect.Collections2$FilteredCollection");

			Set<String> filteredSet = Sets.filter(Set.of("vertifeed"), value -> true);
			filteredSet.isEmpty();
			Map<String, String> filteredMap = Maps.filterKeys(Map.of("vertifeed", "ok"), key -> true);
			filteredMap.entrySet();
			filteredMap.isEmpty();
			Map.copyOf(filteredMap);
		} catch (Throwable t) {
			LOGGER.debug("Guava preload skipped", t);
		}
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
