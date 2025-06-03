# Gradient Descent in the Spacecraft Trajectory Optimization Project

This README provides a high-level overview of how gradient-based optimization—specifically an Adam-based gradient descent—is implemented in this codebase to optimize spacecraft thruster settings (or time-segmented throttle schedules). We cover:

1. **Objective Function**: What is being minimized
2. **Numerical Gradient Estimation**: How ∇f(x) is computed via finite differences
3. **Adam Optimizer**: Core update equations and stopping criteria
4. **Hyperparameters & Defaults**: Their roles and typical values
5. **High-Level Workflow**: How the pieces fit together in practice
6. **References**: Suggested sources for deeper understanding

---

## 1. Objective Function

Every optimization call revolves around a Java function:

```java
Function<double[], Double> objectiveFunction = (double[] thrusterSettings) -> { ... return scalarValue; };
```

* **Input** $x \in \mathbb{R}^n$: A vector of throttle levels for each of the $n$ thrusters, where each $x_i \in [0,1]$
* **Output** $f(x) \in \mathbb{R}$: A scalar cost to minimize, usually:

$$
f(x) = \min_{0 \le t \le T} \|\mathbf{p}_{\text{ship}}(t; x) - \mathbf{p}_{\text{target}}(t)\|
$$

This reflects the closest approach distance to Titan during a simulated trajectory.

### Fuel-Weighted Variant

$$
f(x) = (1 - w) \cdot \min_t d(t) + w \cdot \left(\frac{F_{\text{used}}(x)}{F_{\max}}\right) \cdot \alpha
$$

Where $w \in [0,1]$ balances distance vs. fuel usage.

> **Note**: A single call to `objectiveFunction.apply(x)` runs a full simulation.

---

## 2. Numerical Gradient Estimation

We use **finite differences** to compute gradients:

* **Central difference** (preferred):

$$
\frac{\partial f}{\partial x_i} \approx \frac{f(x + h e_i) - f(x - h e_i)}{2h}
$$

* **One-sided difference** near boundaries:

$$
\frac{\partial f}{\partial x_i} \approx \frac{f(x + h e_i) - f(x)}{h} \quad \text{or} \quad \frac{f(x) - f(x - h e_i)}{h}
$$

* **Zero gradient** if $x_i \pm h \notin [0,1]$

---

## 3. The Adam Optimizer

We use the **Adam optimizer**:

### Notation

* $\alpha$: Learning rate (default 0.1)
* $\beta_1 = 0.9$: First moment decay
* $\beta_2 = 0.999$: Second moment decay
* $\epsilon = 10^{-6}$: Stability term

### Updates

$$
m^{(t)} = \beta_1 m^{(t-1)} + (1 - \beta_1) g^{(t)} \\
v^{(t)} = \beta_2 v^{(t-1)} + (1 - \beta_2) (g^{(t)})^2
$$

Bias correction:

$$
\hat{m}^{(t)} = \frac{m^{(t)}}{1 - \beta_1^t}, \quad \hat{v}^{(t)} = \frac{v^{(t)}}{1 - \beta_2^t}
$$

Update step:

$$
x_i^{(t+1)} = \text{clamp}_{[0,1]} \left(x_i^{(t)} - \alpha \cdot \frac{\hat{m}_i^{(t)}}{\sqrt{\hat{v}_i^{(t)}} + \epsilon} \right)
$$

If no improvement:

$$
\alpha \leftarrow \alpha \cdot \text{learningRateDecay} \quad (\text{default: } 0.95)
$$

### Stopping Criteria

* Stop after max iterations (default: 1000)
* Stop if relative objective change in last 10 steps is < $10^{-6}$

---

## 4. Hyperparameters & Defaults

| Parameter               | Symbol     | Default | Description               |
| ----------------------- | ---------- | ------- | ------------------------- |
| Step size (finite diff) | $h$        | 0.01    | Gradient perturbation     |
| Learning rate           | $\alpha$   | 0.1     | Initial Adam step         |
| First moment decay      | $\beta_1$  | 0.9     | Momentum term             |
| Second moment decay     | $\beta_2$  | 0.999   | RMS-like term             |
| Stability constant      | $\epsilon$ | 1e-6    | Avoid zero division       |
| Max iterations          | $t_{max}$  | 1000    | Convergence limit         |
| Learning rate decay     | $\rho$     | 0.95    | Decay when no improvement |

---

## 5. High-Level Workflow

1. **Create the optimizer**

```java
GradientOptimizer optimizer = new GradientOptimizer();
optimizer.setStepSize(0.02);
optimizer.setLearningRate(0.05);
```

2. **Build the objective**

```java
Function<double[], Double> obj = optimizer.createDistanceObjectiveFunction(
  myShip, myThrusters, targetBody, totalSimTime, timeStep);
```

3. **Run optimization**

```java
GradientOptimizer.GradientDescentResult result =
    optimizer.gradientDescent(obj, initialX, true);
```

4. **Segmented throttle optimization**

```java
optimizer.optimizeThrusterTrajectory(...);
```

---

## 6. Mathematical Summary

* **Cost function**:

$$
f(\mathbf{x}) = \min_t \|\mathbf{p}_{\text{ship}}(t; \mathbf{x}) - \mathbf{p}_{\text{target}}(t)\|
$$

* **Finite difference gradient** (adaptive to bounds)
* **Adam update** (bias-corrected, clamped)
* **Convergence** when relative change $< \delta $

---

## 7. References

* [Central Difference](https://math.stackexchange.com/questions/1398327)
* [Gradient Approximation](https://stackoverflow.com/questions/71859442)
* [StatQuest: Adam Optimizer](https://www.youtube.com/watch?v=JXQT_vxqhE0)
* [Gradient Descent: Downhill to a Minimum- MIT](https://www.youtube.com/watch?v=AeRwohPuUHQ)
* [What Is Mathematical Optimization?](https://www.youtube.com/watch?v=AM6BY4btj-M)

---

**End of README**
