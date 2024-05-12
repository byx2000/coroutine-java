# trampoline

trampoline是一种数据结构，可以实现以下功能：

* 递归函数转非递归
* 生成器（Generator）
* 协程（Coroutine）

## 递归函数转非递归

### 斐波拉契数列

```java
// 传统递归
private long fibonacci1(long n) {
    if (n == 1 || n == 2) {
        return 1;
    }
    return fibonacci1(n - 1) + fibonacci1(n - 2);
}

// 使用trampoline实现非递归
private Trampoline<Long> fibonacci2(long n) {
    if (n == 1 || n == 2) {
        return value(1L);
    }
    return exec(() -> fibonacci2(n - 1))
        .flatMap(a -> exec(() -> fibonacci2(n - 2))
            .map(b -> a + b));
}
```

### 二叉树遍历

前序遍历：

```java
// 传统递归
private void preorderTraverse1(TreeNode node, List<Integer> result) {
    if (node == null) {
        return;
    }

    result.add(node.val);
    preorderTraverse1(node.left, result);
    preorderTraverse1(node.right, result);
}

// 使用trampoline实现非递归
private Trampoline<Void> preorderTraverse2(TreeNode node, List<Integer> result) {
    if (node == null) {
        return empty();
    }

    return exec(() -> result.add(node.val))
        .then(() -> preorderTraverse2(node.left, result))
        .then(() -> preorderTraverse2(node.right, result));
}
```

中序遍历：

```java
// 传统递归
private void inorderTraverse1(TreeNode node, List<Integer> result) {
    if (node == null) {
        return;
    }

    inorderTraverse1(node.left, result);
    result.add(node.val);
    inorderTraverse1(node.right, result);
}

// 使用trampoline实现非递归
private Trampoline<Void> inorderTraverse2(TreeNode node, List<Integer> result) {
    if (node == null) {
        return empty();
    }

    return exec(() -> inorderTraverse2(node.left, result))
        .then(() -> result.add(node.val))
        .then(() -> inorderTraverse2(node.right, result));
}
```

后序遍历：

```java
// 传统递归
private void postorderTraverse1(TreeNode node, List<Integer> result) {
    if (node == null) {
        return;
    }

    postorderTraverse1(node.left, result);
    postorderTraverse1(node.right, result);
    result.add(node.val);
}

// 使用trampoline实现非递归
private Trampoline<Void> postorderTraverse2(TreeNode node, List<Integer> result) {
    if (node == null) {
        return empty();
    }

    return exec(() -> postorderTraverse2(node.left, result))
        .then(() -> postorderTraverse2(node.right, result))
        .then(() -> result.add(node.val));
}
```

## 生成器（Generator）

### 斐波拉契数列生成器

```java
private Generator<Integer> fibonacciGenerator(int a, int b) {
    AtomicInteger x = new AtomicInteger(a);
    AtomicInteger y = new AtomicInteger(b);

    return pause(a)
        .loop(
            () -> true,
            () -> pause(y.get()).then(() -> {
                int t = x.get();
                x.set(y.get());
                y.set(t + y.get());
            })
        )
        .toGenerator();
}

Generator<Integer> generator = fibonacciGenerator(1, 2);
List<Integer> nums = generator.stream().limit(10).toList();
assertEquals(List.of(1, 2, 3, 5, 8, 13, 21, 34, 55, 89), nums);
```

### 全排列生成器

```java
private Generator<List<Integer>> permutationGenerator(int[] nums) {
    return permutation(0, nums, new boolean[nums.length], new LinkedList<>()).toGenerator();
}

private Trampoline<Void> permutation(int index, int[] nums, boolean[] flag, LinkedList<Integer> p) {
    if (index == nums.length) {
        return pause(new ArrayList<>(p));
    }

    return loop(0, nums.length, i -> {
        if (!flag[i]) {
            return exec(() -> flag[i] = true)
                .then(() -> p.addLast(nums[i]))
                .then(() -> permutation(index + 1, nums, flag, p))
                .then(() -> p.removeLast())
                .then(() -> flag[i] = false);
        }
        return empty();
    });
}

Generator<List<Integer>> generator = permutationGenerator(new int[]{1, 2, 3});
assertEquals(List.of(
    List.of(1, 2, 3),
    List.of(1, 3, 2),
    List.of(2, 1, 3),
    List.of(2, 3, 1),
    List.of(3, 1, 2),
    List.of(3, 2, 1)
), generator.stream().toList());
```

## 协程（Coroutine）

### 计数器

```java
private Coroutine countDown(int n) {
    AtomicInteger cnt = new AtomicInteger(n);
    return loop(
        () -> cnt.get() > 0,
        () -> pause(cnt.get(), Integer.class).then(reset -> {
            if (reset != null) {
                cnt.set(reset);
            } else {
                cnt.decrementAndGet();
            }
        })
    ).toCoroutine();
}

Coroutine co = countDown(5);
assertEquals(5, (int) co.run());
assertEquals(4, (int) co.run());
assertEquals(10, (int) co.run(10));
assertEquals(9, (int) co.run());
assertEquals(8, (int) co.run());
assertEquals(7, (int) co.run());
assertEquals(15, (int) co.run(15));
assertEquals(14, (int) co.run());
assertEquals(13, (int) co.run());
assertEquals(12, (int) co.run());
```
