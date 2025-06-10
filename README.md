# Group Project 15: Project 1-2 – A Titanic Space Odyssey

---
## Execution

### Prerequisites

* Java JDK 11 or higher installed on your system
* (Optional) Maven 3.6+ for build automation

### Building the Project

**Using Maven**

```bash
# From the project root directory:
mvn clean package
```
### Running the Components

After building, you can run each part of the project as follows:

* **GUI (JavaFX Interface)**

  ```bash
  #using Maven:
  mvn exec:java -Dexec.mainClass="com.example.coreGui.SolvonautGUI"
  ```

* **Genetic Algorithm (GA)**

  ```bash
  #using Maven:
  mvn exec:java -Dexec.mainClass="com.example.utilities.GA.GeneticTitan"
  ```

* **Hill Climber**

  ```bash
  # If using Maven:
  mvn exec:java -Dexec.mainClass="com.example.utilities.HillClimb.TitanInsertionHillClimbing"

  ```
---

# Project description
The objective was to build a coherent system capable of simulating an end-to-end mission, from launch to atmospheric descent, by combining numerical solvers, optimization algorithms, active control mechanisms and a 3D JavaFX interface for real-time visualization.

The solar system is modelled as a collection of gravitationally interacting bodies. To ensure accuracy and flexibility across components, we implemented and compared three classical ODE solvers: Euler, RK4, and RKF45. After benchmarking, RK4 was selected as the default integrator for both planetary motion and the probe’s trajectory, offering a good compromise between numerical stability, implementation complexity, and computational efficiency.

To plan a passive transfer to Titan, we used a Genetic Algorithm (GA) that evolves a population of candidate solutions, each defined by a 6-dimensional vector encoding the probe’s initial position and velocity. These candidates are evaluated by simulating a one-year gravity-only journey. Despite the complexity of the system, the GA consistently found trajectories that led the probe to collide directly with Titan, demonstrating that well-tuned launch conditions alone can achieve interplanetary transfer. The algorithm also filters out unrealistic paths that would intersect with other celestial bodies.

To go beyond passive collision, we implemented an orbital insertion step that applies small thrusts to guide the probe into a stable orbit around Titan. This phase starts from the best individual found by the Genetic Algorithm and refines it using a hill climbing algorithm. The hill climber applies discrete thrust variations to that solution, adjusting timing and direction, and keeps the one that improves orbital stability the most. Each thrust-adjusted trajectory is evaluated using the RKF45 integrator to simulate its orbital outcome with high precision. By iteratively testing and selecting the best local adjustment, the algorithm finds a feasible thrust combination that allows orbital capture without exceeding fuel constraints.

We also simulate the probe’s descent through Titan’s dense atmosphere, where drag is modeled as a force opposing the lander’s motion. To control the descent and avoid a crash, we use a combined controller that blends predefined thrust planning with real-time feedback. The open-loop component schedules braking phases, while the feedback part adjusts thrust and tilt based on the probe’s state. This setup captures key aspects of atmospheric entry and enables safe landings under a variety of conditions, including moderate wind and uneven terrain.

To support development and analysis, we built a 3D JavaFX-based GUI that visualizes the real-time motion of the Sun, planets, Titan, and the probe. This interface was valuable for debugging, verifying trajectories, and understanding orbital dynamics.

Altogether, this project combines simulation, optimization, and control into a modular system capable of modelling a realistic (if simplified) space mission from Earth to Titan’s surface.
