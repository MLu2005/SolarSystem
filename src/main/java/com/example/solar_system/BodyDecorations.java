package com.example.solar_system;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.effect.Bloom;
import javafx.scene.shape.Sphere;
import java.util.HashMap;
import java.util.Map;

/*
 * provides visual styles and decorations for celestial bodies
 * in a solar system simulation. It includes colors, materials (textures and lighting),
 * scaled sizes, and optional visual effects like glow.
 */
public class BodyDecorations {

    private static final Map<String, MaterialProvider> MATERIAL_PROVIDERS = new HashMap<>();


    /**
     * Assigns a color to each planet and moon.
     *
     * @param name the celestial body name
     * @return color for the body
     */
    public static Color getColor(String name) {
        return switch (name.toLowerCase()) {
            case "sun" -> Color.GOLD;
            case "mercury" -> Color.SILVER;
            case "venus" -> Color.BURLYWOOD;
            case "earth" -> Color.BLUE;
            case "moon" -> Color.LIGHTGRAY;
            case "mars" -> Color.RED;
            case "jupiter" -> Color.ORANGE;
            case "saturn" -> Color.BEIGE;
            case "titan" -> Color.DARKKHAKI;
            case "uranus" -> Color.AQUA;
            case "neptune" -> Color.DARKBLUE;
            default -> Color.WHITE;
        };
    }

    /**
     * Provides a scaled visual radius for a celestial body.
     *
     * @param name the celestial body name
     * @return the scaled radius
     */
    public static double getScaledRadius(String name) {
        return switch (name.toLowerCase()) {
            case "sun" -> 100.0;
            case "mercury" -> 5.0;
            case "venus" -> 9.0;
            case "earth" -> 10.0;
            case "moon" -> 2.5;
            case "mars" -> 6.5;
            case "jupiter" -> 34.0;
            case "saturn" -> 25.0;
            case "titan" -> 6.0;
            case "uranus" -> 20.0;
            case "neptune" -> 17.0;
            default -> 5.0;
        };
    }

    static {

        /* Each celestial body is assigned a texture, color, and lighting properties
         *  Moon/titan from 5 <> 8 (specularPower)
         *  Planets from 16 <> 32 (specularPower)
         */
        MATERIAL_PROVIDERS.put("sun", () -> {
            PhongMaterial rockMaterial = new PhongMaterial(Color.GOLD);
            try {
                Image glowTexture = new Image(
                        BodyDecorations.class.getResource("/styles/solarSystemStyling/sunMesh2.png").toExternalForm()
                );
                rockMaterial.setDiffuseMap(glowTexture);
                rockMaterial.setSelfIlluminationMap(glowTexture);
            } catch (Exception e) {
                System.err.println("Failed to load sun glow texture: " + e.getMessage());
            }
            rockMaterial.setSpecularColor(Color.YELLOW);
            rockMaterial.setDiffuseColor(Color.rgb(255, 215, 0));
            return rockMaterial;
        });

        MATERIAL_PROVIDERS.put("earth", () -> {
            PhongMaterial rockMaterial = new PhongMaterial();
            try {
                Image earthTexture = new Image(
                        BodyDecorations.class.getResource("/styles/solarSystemStyling/earthMesh.png").toExternalForm()
                );
                rockMaterial.setDiffuseMap(earthTexture);
            } catch (Exception e) {
                System.err.println("Failed to load earth texture: " + e.getMessage());
                rockMaterial.setDiffuseColor(Color.BLUE);
            }
            rockMaterial.setSpecularColor(Color.LIGHTBLUE);
            rockMaterial.setSpecularPower(25);
            return rockMaterial;
        });

        MATERIAL_PROVIDERS.put("moon", () -> {
            PhongMaterial rockMaterial = new PhongMaterial();
            try {
                Image earthTexture = new Image(
                        BodyDecorations.class.getResource("/styles/solarSystemStyling/moonMesh.png").toExternalForm()
                );
                rockMaterial.setDiffuseMap(earthTexture);
            } catch (Exception e) {
                System.err.println("Failed to load moon texture: " + e.getMessage());
                rockMaterial.setDiffuseColor(Color.GRAY);
            }
            rockMaterial.setSpecularColor(Color.DIMGRAY);
            rockMaterial.setSpecularPower(8);
            return rockMaterial;
        });

        MATERIAL_PROVIDERS.put("mercury", () -> {
            PhongMaterial rockMaterial = new PhongMaterial();
            try {
                Image earthTexture = new Image(
                        BodyDecorations.class.getResource("/styles/solarSystemStyling/mercuryMesh.png").toExternalForm()
                );
                rockMaterial.setDiffuseMap(earthTexture);
            } catch (Exception e) {
                System.err.println("Failed to load mercury texture: " + e.getMessage());
                rockMaterial.setDiffuseColor(Color.SILVER);
            }
            rockMaterial.setSpecularColor(Color.SILVER);
            rockMaterial.setSpecularPower(16);
            return rockMaterial;
        });

        MATERIAL_PROVIDERS.put("mars", () -> {
            PhongMaterial rockMaterial = new PhongMaterial();
            try {
                Image earthTexture = new Image(
                        BodyDecorations.class.getResource("/styles/solarSystemStyling/marsMesh.png").toExternalForm()
                );
                rockMaterial.setDiffuseMap(earthTexture);
            } catch (Exception e) {
                System.err.println("Failed to load mars texture: " + e.getMessage());
                rockMaterial.setDiffuseColor(Color.RED);
            }
            rockMaterial.setSpecularColor(Color.RED);
            rockMaterial.setSpecularPower(21);
            return rockMaterial;
        });

        MATERIAL_PROVIDERS.put("saturn", () -> {
            PhongMaterial rockMaterial = new PhongMaterial();
            try {
                Image earthTexture = new Image(
                        BodyDecorations.class.getResource("/styles/solarSystemStyling/saturnMesh.png").toExternalForm()
                );
                rockMaterial.setDiffuseMap(earthTexture);
            } catch (Exception e) {
                System.err.println("Failed to load saturn texture: " + e.getMessage());
                rockMaterial.setDiffuseColor(Color.BEIGE);
            }
            rockMaterial.setSpecularColor(Color.BEIGE);
            rockMaterial.setSpecularPower(25);
            return rockMaterial;
        });

        MATERIAL_PROVIDERS.put("uranus", () -> {
            PhongMaterial rockMaterial = new PhongMaterial();
            try {
                Image earthTexture = new Image(
                        BodyDecorations.class.getResource("/styles/solarSystemStyling/uranusMesh.png").toExternalForm()
                );
                rockMaterial.setDiffuseMap(earthTexture);
            } catch (Exception e) {
                System.err.println("Failed to load uranus texture: " + e.getMessage());
                rockMaterial.setDiffuseColor(Color.AQUA);
            }
            rockMaterial.setSpecularColor(Color.AQUA);
            rockMaterial.setSpecularPower(23);
            return rockMaterial;
        });

        MATERIAL_PROVIDERS.put("neptune", () -> {
            PhongMaterial rockMaterial = new PhongMaterial();
            try {
                Image earthTexture = new Image(
                        BodyDecorations.class.getResource("/styles/solarSystemStyling/neptuneMesh.png").toExternalForm()
                );
                rockMaterial.setDiffuseMap(earthTexture);
            } catch (Exception e) {
                System.err.println("Failed to load neptune texture: " + e.getMessage());
                rockMaterial.setDiffuseColor(Color.BLUE);
            }
            rockMaterial.setSpecularColor(Color.BLUE);
            rockMaterial.setSpecularPower(29);
            return rockMaterial;
        });

        MATERIAL_PROVIDERS.put("jupiter", () -> {
            PhongMaterial rockMaterial = new PhongMaterial();
            try {
                Image earthTexture = new Image(
                        BodyDecorations.class.getResource("/styles/solarSystemStyling/jupiterMesh.png").toExternalForm()
                );
                rockMaterial.setDiffuseMap(earthTexture);
            } catch (Exception e) {
                System.err.println("Failed to load jupiter texture: " + e.getMessage());
                rockMaterial.setDiffuseColor(Color.ORANGE);
            }
            rockMaterial.setSpecularColor(Color.ORANGE);
            rockMaterial.setSpecularPower(23);
            return rockMaterial;
        });

        MATERIAL_PROVIDERS.put("titan", () -> {
            PhongMaterial rockMaterial = new PhongMaterial();
            try {
                Image earthTexture = new Image(
                        BodyDecorations.class.getResource("/styles/solarSystemStyling/titanMesh.png").toExternalForm()
                );
                rockMaterial.setDiffuseMap(earthTexture);
            } catch (Exception e) {
                System.err.println("Failed to load titan texture: " + e.getMessage());
                rockMaterial.setDiffuseColor(Color.DARKKHAKI);
            }
            rockMaterial.setSpecularColor(Color.DARKKHAKI);
            rockMaterial.setSpecularPower(7);
            return rockMaterial;
        });

        MATERIAL_PROVIDERS.put("venus", () -> {
            PhongMaterial rockMaterial = new PhongMaterial();
            try {
                Image earthTexture = new Image(
                        BodyDecorations.class.getResource("/styles/solarSystemStyling/venusMesh.png").toExternalForm()
                );
                rockMaterial.setDiffuseMap(earthTexture);
            } catch (Exception e) {
                System.err.println("Failed to load venus texture: " + e.getMessage());
                rockMaterial.setDiffuseColor(Color.BURLYWOOD);
            }
            rockMaterial.setSpecularColor(Color.BURLYWOOD);
            rockMaterial.setSpecularPower(29);
            return rockMaterial;
        });


    }

    /**
     * Used for customizing the rocket by taking in multiple skins for each body structure.
     *
     * @param fallbackColor backup color.
     * @param images eh guiStyling?
     */
    public static PhongMaterial createMaterialFromImages(Color fallbackColor, Image... images) {
        PhongMaterial material = new PhongMaterial();

        for (Image img : images) {
            if (img != null && !img.isError()) {
                material.setDiffuseMap(img);
                return material;
            }
        }

        // fallback if none valid
        material.setDiffuseColor(fallbackColor);
        return material;
    }




    /**
     * Returns the PhongMaterial (includes texture and lighting info)
     * for a celestial body. If no specific material is found,
     * a default one is created using the body's color.
     *
     * @param name the name of the celestial body
     * @return the visual material for the body
     */
    public static PhongMaterial getMaterial(String name) {
        MaterialProvider provider = MATERIAL_PROVIDERS.get(name.toLowerCase());

        if (provider != null) {
            return provider.createMaterial();
        } else {
            // Default simple material
            PhongMaterial rockMaterial = new PhongMaterial(getColor(name));
            rockMaterial.setSpecularColor(Color.GRAY);
            rockMaterial.setSpecularPower(16);
            return rockMaterial;
        }
    }



    /**
     * Adds a glowing effect to a sphere if it's representing the Sun.
     *
     * @param sphere the sphere representing a celestial body
     * @param name the name of the celestial body
     */
    public static void applyGlowEffectIfSun(Sphere sphere, String name) {
        if (name.equalsIgnoreCase("sun")) {
            Bloom bloom = new Bloom();
            bloom.setThreshold(0.02); // lower = more intense glow
            sphere.setEffect(bloom);
        }
    }

}
