package com.moromoro.heliopause.recipe;

import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.moromoro.Heliopause;
import com.moromoro.heliopause.blockEntity.AbstractWrittenBoardBlockEntity;
import com.moromoro.heliopause.blockEntity.WrittenBoardBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class MagicCircleAssemblyRecipe implements Recipe<Container> {

    // データクラス定義
    public record Trigger(String type, Boolean isBlock, ResourceLocation blockOrItem) {}
    public record Result(Boolean isBlock, ResourceLocation blockOrItem){}
    public record Node(String key, List<String> connects){}
    public record Circle(String key, String center, List<String> contains){}

    // 文字列定義
    public static final String ORIGIN_KEY = "ORIGIN";

    // レシピパラメータ
    private final ResourceLocation recipeId;
    private final Trigger trigger;
    private final Result result;
    private final NonNullList<Node> nodes;
    private final NonNullList<Circle> circles;

    public MagicCircleAssemblyRecipe(Trigger trigger, Result result, NonNullList<Node> parts, NonNullList<Circle> circles, ResourceLocation recipeId) {
        this.recipeId = recipeId;
        this.trigger = trigger;
        this.result = result;
        this.nodes = parts;
        this.circles = circles;
    }

    public static class Type implements RecipeType<MagicCircleAssemblyRecipe>{
        public static final MagicCircleAssemblyRecipe.Type INSTANCE = new MagicCircleAssemblyRecipe.Type();
        public static final String ID = "magic_circle_assembly";
    }

    public static class Serializer implements RecipeSerializer<MagicCircleAssemblyRecipe> {
        public static final MagicCircleAssemblyRecipe.Serializer INSTANCE = new MagicCircleAssemblyRecipe.Serializer();
        public static final ResourceLocation ID = new ResourceLocation(Heliopause.MODID, MagicCircleAssemblyRecipe.Type.ID);

        @Override
        public @NotNull MagicCircleAssemblyRecipe fromJson(@NotNull ResourceLocation recipeId, JsonObject json) {
            // トリガーの取得
            JsonObject triggerObject = json.getAsJsonObject("trigger");
            String triggerType = triggerObject.get("type").getAsString();
            // タイプがブロックのときはブロックを、それ以外はアイテムを取得する
            boolean triggerIsBlock = triggerObject.has("block");
            ResourceLocation triggerResource =
                new ResourceLocation(triggerObject.has("block") ?
                        triggerObject.get("block").getAsString() :
                        triggerObject.get("item").getAsString()
                );

            // 結果の取得
            JsonObject result = json.getAsJsonObject("result");
            boolean resultIsBlock = triggerObject.has("block");
            ResourceLocation resultResource = new ResourceLocation(resultIsBlock ?
                result.get("block").getAsString() :
                result.get("item").getAsString()
            );

            // パーツの取得
            NonNullList<Node> recipeNodes = NonNullList.create();//new ArrayList<>();
            NonNullList<Circle> recipeCircles = NonNullList.create();
            for(JsonElement jsonElement : json.getAsJsonArray("parts")) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                switch (jsonObject.get("type").getAsString()){
                    case "node":{
                        recipeNodes.add(new Node(jsonObject.get("key").getAsString(),
                            Streams.stream(jsonObject.getAsJsonArray("contains")).map(JsonElement::getAsString).toList()));
                        break;
                    }
                    case "circle":{
                        recipeCircles.add(new Circle(jsonObject.get("key").getAsString(),jsonObject.get("center").getAsString(),
                            Streams.stream(jsonObject.getAsJsonArray("contains")).map(JsonElement::getAsString).toList()));
                        break;
                    }
                    /*default:{

                    }*/
                }
                /*parts.add(new Part(jsonObject.get("type").getAsString(), jsonObject.get("key").getAsString(),
                    jsonObject.has("center") ? jsonObject.get("center").getAsString() : "",
                    Streams.stream(jsonObject.getAsJsonArray("contains")).map(JsonElement::getAsString).toList()
                    ));*/
            }

            return new MagicCircleAssemblyRecipe(new Trigger(triggerType, triggerIsBlock, triggerResource), new Result(resultIsBlock, resultResource), recipeNodes, recipeCircles, recipeId);
        }

        @Override
        public @Nullable MagicCircleAssemblyRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Heliopause.LOGGER.debug("read from network, {}", recipeId);
            String triggerType = buffer.readUtf();
            Boolean triggerIsBlock = buffer.readBoolean();
            ResourceLocation triggerResource = buffer.readResourceLocation();
            Boolean resultIsBlock = buffer.readBoolean();
            ResourceLocation resultResource = buffer.readResourceLocation();

            NonNullList<Node> recipeNodes = NonNullList.create();
            int nodeCount = buffer.readInt();
            for (int nodeId = 0; nodeId < nodeCount; nodeId++) {
                String key = buffer.readUtf();
                int connectCount = buffer.readInt();
                List<String> connects = new ArrayList<>();
                for (int connectId = 0; connectId < connectCount; connectId++) {
                    connects.add(buffer.readUtf());
                }
                recipeNodes.add(new Node(key, connects));
            }
            NonNullList<Circle> recipeCircles = NonNullList.create();
            int circleCount = buffer.readInt();
            for (int circleId = 0; circleId < circleCount; circleId++) {
                String key = buffer.readUtf();
                String center = buffer.readUtf();
                int containCount = buffer.readInt();
                List<String> contains = new ArrayList<>();
                for (int containId = 0; containId < containCount; containId++) {
                    contains.add(buffer.readUtf());
                }
                recipeCircles.add(new Circle(key, center, contains));
            }

            return new MagicCircleAssemblyRecipe(new Trigger(triggerType, triggerIsBlock, triggerResource), new Result(resultIsBlock, resultResource), recipeNodes, recipeCircles, recipeId);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, MagicCircleAssemblyRecipe recipe) {
            buffer.writeUtf(recipe.getTrigger().type());
            buffer.writeBoolean(recipe.getTrigger().isBlock());
            buffer.writeResourceLocation(recipe.getTrigger().blockOrItem());
            buffer.writeBoolean(recipe.getResult().isBlock());
            buffer.writeResourceLocation(recipe.getResult().blockOrItem());
            NonNullList<Node> recipeNodes = recipe.getNodes();
            NonNullList<Circle> recipeCircles = recipe.getCircles();

            buffer.writeInt(recipeNodes.size());
            for (Node node : recipeNodes) {
                buffer.writeUtf(node.key());
                buffer.writeInt(node.connects().size());
                for (String connect : node.connects()) {
                    buffer.writeUtf(connect);
                }
            }

            buffer.writeInt(recipeCircles.size());
            for (Circle circle : recipeCircles) {
                buffer.writeUtf(circle.key());
                buffer.writeUtf(circle.center());
                buffer.writeInt(circle.contains().size());
                for (String contain : circle.contains()) {
                    buffer.writeUtf(contain);
                }
            }
            //buffer.writeInt(recipe.getNodes().size());
            //buffer.writeInt(recipe.getParts().size());
            /*for(Part part : recipe.getParts()){
                buffer.writeUtf(part.type());
                buffer.writeUtf(part.key());
                buffer.writeUtf(part.center()==null ? "" : part.center());
                buffer.writeInt(part.contains().size());
                for(String c : part.contains()) buffer.writeUtf(c);
            }*/
        }

    }

    private record levelNode(BlockPos keyPos, List<BlockPos> connectsPos){}
    private record levelCircle(double keyRadius, BlockPos centerPos, List<BlockPos> containsPos){}
    // ネットワークの比較
    public static @Nullable List<BlockPos> matchesAt(Level level, BlockPos originPos, MagicCircleAssemblyRecipe recipe){
        // ルート位置のブロックエンティティを取得
        BlockEntity blockEntity = level.getBlockEntity(originPos);
        if (!(blockEntity instanceof WrittenBoardBlockEntity originBlockEntity)) {
            return null;
        }
        // ルートじゃないならキャンセル
        if (!originBlockEntity.isRoot(level) || originBlockEntity.isFunctionalRoot(level)) {
            return null;
        }

        // レシピのノードを取得
        NonNullList<Node> recipeNodes = recipe.getNodes();
        NonNullList<Circle> recipeCircles = recipe.getCircles();

        // ネットワークからレシピに記載されるべきノードを取得
        Set<levelNode> levelWholeNodes = getLevelNodes(level, originBlockEntity, originPos, recipeNodes.size() + 1);
        //Set<levelNode> levelNodes = getConnectedLevelNodes(level, levelWholeNodes);
        Set<levelCircle> levelCircles = getLevelCircles(level, levelWholeNodes);
        Optional<levelNode> originNodeOptional = levelWholeNodes.stream().filter(levelNode -> levelNode.keyPos().equals(originPos)).findFirst();
        // 中心ノードが繋がりを持たない場合false
        if(originNodeOptional.isEmpty()){
            return null;
        }
        levelNode originNode = originNodeOptional.get();
        levelWholeNodes.remove(originNode);

        // ノードの数チェック
        if(levelWholeNodes.size() != recipeNodes.size() || levelCircles.size() != recipeCircles.size()){
            return null;
        }

        // 構造チェック
        return checkStructure(levelWholeNodes, levelCircles, originNode, recipeNodes, recipeCircles);
    }

    private record nodeCandidate(Node node, List<levelNode> candidateLevelNodes){}

    private static @Nullable List<BlockPos> checkStructure(Set<levelNode> levelNodes, Set<levelCircle> levelCircles, levelNode originNode, NonNullList<Node> recipeNodes, NonNullList<Circle> recipeCircles) {
        // 枝分かれ数チェック
        List<nodeCandidate> candidatesByLinePair = new ArrayList<>();
        for (Node recipeNode : recipeNodes) {
            int recipePairCount = recipeNode.connects().size();
            List<levelNode> candidateLevelNodes = levelNodes.stream().filter(levelNode -> levelNode.connectsPos().size() == recipePairCount).toList();
            // 一致がないなら終了
            if(candidateLevelNodes.isEmpty()){
                return null;
            }
            // 候補リスト
            candidatesByLinePair.add(new nodeCandidate(recipeNode, candidateLevelNodes));
        }

        // 円の数チェック
        List<nodeCandidate> candidatesByCircle = new ArrayList<>();
        for (nodeCandidate nodeCandidate : candidatesByLinePair) {
            Node node = nodeCandidate.node();
            int recipeCircleCount = recipeCircles.stream().filter(Circle -> Circle.center().equals(node.key())).toList().size();
            List<levelNode> candidateLevelNodes = new ArrayList<>();
            for (levelNode levelNode : nodeCandidate.candidateLevelNodes()) {
                int levelCircleCount = levelCircles.stream().filter(levelCircle -> levelCircle.centerPos.equals(levelNode.keyPos())).toList().size();
                if(levelCircleCount == recipeCircleCount){
                    candidateLevelNodes.add(levelNode);
                }
            }
            // 一致がないなら終了
            if(candidateLevelNodes.isEmpty()){
                return null;
            }
            candidatesByCircle.add(new nodeCandidate(node, candidateLevelNodes));
        }

        // 所属円の数チェック
        List<nodeCandidate> candidatesByCenter = new ArrayList<>();
        for (nodeCandidate nodeCandidate : candidatesByCircle) {
            Node node = nodeCandidate.node();
            int recipeCenterCount = recipeCircles.stream().filter(Circle -> Circle.contains().contains(node.key())).toList().size();
            List<levelNode> candidateLevelNodes = new ArrayList<>();
            for (levelNode levelNode : nodeCandidate.candidateLevelNodes()) {
                int levelCenterCount = levelCircles.stream().filter(levelCircle -> levelCircle.containsPos.contains(levelNode.keyPos())).toList().size();
                if(levelCenterCount == recipeCenterCount){
                    candidateLevelNodes.add(levelNode);
                }
            }
            // 一致がないなら終了
            if(candidateLevelNodes.isEmpty()){
                return null;
            }
            candidatesByCenter.add(new nodeCandidate(node, candidateLevelNodes));
        }

        // 候補をキーマップに変換
        Map<String, List<levelNode>> candidatesByKey = new HashMap<>();
        for (nodeCandidate nodeCandidate : candidatesByCenter) {
            candidatesByKey.put(nodeCandidate.node().key(), new ArrayList<>(nodeCandidate.candidateLevelNodes()));
        }

        // 割り当てキーマップを作成
        List<String> keyList = new ArrayList<>(candidatesByKey.keySet());
        keyList.sort(Comparator.comparingInt(key -> candidatesByKey.get(key).size()));

        // 中心ノードを追加
        candidatesByKey.put(ORIGIN_KEY, List.of(originNode));

        // ノードとキーの全単射チェック
        Map<String, levelNode> assignMap = new HashMap<>(Map.of(ORIGIN_KEY, originNode));
        if (tryAssign(0, keyList, candidatesByKey, assignMap, new HashSet<>(List.of(originNode)), recipeNodes, levelCircles, recipeCircles)){
            return assignMap.values().stream().map(levelNode::keyPos).toList();
        }
        return null;
    }

    // 再帰的に全単射チェック
    private static boolean tryAssign(int nodeId, List<String> keyList, Map<String, List<levelNode>> candidatesMap, Map<String, levelNode> assignMap,
        Set<levelNode> usedLevelNodes, NonNullList<Node> recipeNodes, Set<levelCircle> levelCircles, NonNullList<Circle> recipeCircles) {
        if (nodeId >= keyList.size()) {
            for (Node recipeNode : recipeNodes) {
                levelNode levelNode = assignMap.get(recipeNode.key());
                // 接続できていないものがあるなら失敗
                if (!verifyConnection(assignMap, recipeNode, levelNode)) {
                    return false;
                }
            }
            return verifyCircles(assignMap, levelCircles, recipeCircles);
        }

        String key = keyList.get(nodeId);
        Node recipeNode = recipeNodes.stream().filter(n -> n.key().equals(key)).findFirst().orElse(null);
        if (recipeNode != null) {
            for (levelNode nodeCandidate : candidatesMap.get(key)) {
                // 割り当て済みならチェックしない
                if (!usedLevelNodes.add(nodeCandidate)) {
                    continue;
                }
                // 接続しうるならチェック
                if (verifyConnection(assignMap, recipeNode, nodeCandidate)) {
                    assignMap.put(key, nodeCandidate);
                    if (tryAssign(nodeId + 1, keyList, candidatesMap, assignMap, usedLevelNodes, recipeNodes, levelCircles, recipeCircles))
                        return true;
                    assignMap.remove(key);
                }
                usedLevelNodes.remove(nodeCandidate);
            }
        }
        return false;
    }

    // 接続のチェック
    private static boolean verifyConnection(Map<String, levelNode> assignMap, Node recipeNode, levelNode levelNode) {
        for (String connectsKey : recipeNode.connects()) {
            levelNode connectLevelNode = assignMap.get(connectsKey);
            if (connectLevelNode != null && !levelNode.connectsPos().contains(connectLevelNode.keyPos())) return false;
        }
        return true;
    }

    // 円のチェック
    private static boolean verifyCircles(Map<String, levelNode> assignMap, Set<levelCircle> levelCircles, NonNullList<Circle> recipeCircles) {
        List<levelCircle> levelCircleList = new ArrayList<>(levelCircles);
        boolean[] used = new boolean[levelCircleList.size()];

        // 円ごとに円周上のノード一致確認
        for (Circle recipeCircle : recipeCircles) {
            BlockPos centerPos = assignMap.get(recipeCircle.center()).keyPos();
            Set<BlockPos> required = recipeCircle.contains().stream().map(key -> assignMap.get(key).keyPos()).collect(Collectors.toSet());

            boolean matched = false;
            for (int i = 0; i < levelCircleList.size(); i++) {
                if (used[i]) continue;
                levelCircle levelCircle = levelCircleList.get(i);
                if (levelCircle.centerPos().equals(centerPos) && new HashSet<>(levelCircle.containsPos()).equals(required)) {
                    used[i] = true;
                    matched = true;
                    break;
                }
            }
            if (!matched) return false;
        }
        return true;
    }

    // ネットワークの全ノード取得
    private static HashSet<levelNode> getLevelNodes(Level level, WrittenBoardBlockEntity rootBlockEntity, BlockPos rootPos, int maxNodeCount){
        // 繋がりがないなら比較用セットに追加しない
        if(rootBlockEntity.getLinePairs().isEmpty() && rootBlockEntity.getCircleRadii().isEmpty()){
            return new HashSet<>();
        }

        HashSet<levelNode> nodeSet = new HashSet<>();
        Set<BlockPos> visitedNodePos = new HashSet<>();
        Queue<WrittenBoardBlockEntity> queueBlockEntities = new ArrayDeque<>();
        queueBlockEntities.add(rootBlockEntity);
        visitedNodePos.add(rootPos);

        while (!queueBlockEntities.isEmpty()){
            WrittenBoardBlockEntity blockEntity = queueBlockEntities.poll();
            // 繋がりがないならキューには追加しない
            if(blockEntity.getLinePairs().isEmpty() && blockEntity.getCircleRadii().isEmpty() && blockEntity.getCircleCenters().size() < 2){
                continue;
            }
            nodeSet.add(new levelNode(blockEntity.getBlockPos(), new ArrayList<>(blockEntity.getLinePairs())));

            // レシピの要求ノード数を越えたら探索終了
            if(nodeSet.size() > maxNodeCount){
                return nodeSet;
            }

            // 繋がるノードのエンティティから
            for (BlockPos linePair : blockEntity.getLinePairs()) {
                // 既に追加されているものはスキップ
                if(visitedNodePos.contains(linePair)){
                    continue;
                }
                if(level.getBlockEntity(linePair) instanceof WrittenBoardBlockEntity pairEntity){
                    if(pairEntity.getRootPos(level, linePair).equals(rootPos)){
                        // 探索済みに追加
                        visitedNodePos.add(linePair);
                        queueBlockEntities.add(pairEntity);
                    }
                }
            }

            // 円周上のエンティティから
            for (Double circleRadius : blockEntity.getCircleRadii()) {
                for (BlockPos latticePos : AbstractWrittenBoardBlockEntity.getCircleLatticePos(blockEntity.getBlockPos(), circleRadius)) {
                    // 既に追加されているものはスキップ
                    if(visitedNodePos.contains(latticePos)){
                        continue;
                    }
                    if(level.getBlockEntity(latticePos) instanceof WrittenBoardBlockEntity pairEntity){
                        if(pairEntity.getRootPos(level, latticePos).equals(rootPos)){
                            // 探索済みに追加
                            visitedNodePos.add(latticePos);
                            queueBlockEntities.add(pairEntity);
                        }
                    }
                }
            }
        }

        return nodeSet;
    }

    // 全ノードのうち、接続があるものを返す
    private static Set<levelNode> getConnectedLevelNodes(Level level, Set<levelNode> levelWholeNodes){
        HashSet<levelNode> nodeSet = new HashSet<>();
        for (levelNode node : levelWholeNodes) {
            if(!node.connectsPos().isEmpty()){
                nodeSet.add(node);
            }
        }
        return nodeSet;
    }

    // 全ノードのうち、円があるものを返す
    private static Set<levelCircle> getLevelCircles(Level level, Set<levelNode> levelWholeNodes){
        HashSet<levelCircle> circleSet = new HashSet<>();
        for (levelNode node : levelWholeNodes) {
            BlockPos nodePos = node.keyPos();
            if(level.getBlockEntity(nodePos) instanceof WrittenBoardBlockEntity nodeEntity){
                for (Double circleRadius : nodeEntity.getCircleRadii()) {
                    List<BlockPos> latticePos = AbstractWrittenBoardBlockEntity.getCircleLatticePos(nodePos, circleRadius);
                    // latticePosをキーに持つノードを探す
                    List<BlockPos> containsPos = levelWholeNodes.stream()
                        .map(levelNode::keyPos).filter(latticePos::contains).toList();

                    circleSet.add(new levelCircle(circleRadius, nodePos, containsPos));
                }
            }
        }
        return circleSet;
    }

    /*private static List<blockTravel> getChildWithPassed(Level level, BlockPos pos, WrittenBoardBlockEntity originBlockEntity, int depth, int maxDepth) {
        if(depth <= maxDepth){
            List<blockTravel> blockTravels = new ArrayList<>();
            for (double circleRadius : originBlockEntity.getCircleRadii()) {
                for (BlockPos latticePos : AbstractWrittenBoardBlockEntity.getCircleLatticePos(pos,circleRadius)) {
                    if(level.getBlockEntity(latticePos) instanceof WrittenBoardBlockEntity childEntity){
                        List<blockTravel> circleTravels = new ArrayList<>(getChildWithPassed(level, pos, childEntity, depth + 1, maxDepth));
                        // 円周上で繋がりがひとつもないなら
                        if(circleTravels.size() <= 1){
                            continue;
                        }
                        for (blockTravel circleTravel : circleTravels) {
                            circleTravel.passed.add(pos);
                            circleTravel.type.add("circle" + circleRadius);
                        }
                        blockTravels.addAll(circleTravels);
                    }
                }
            }
            for (BlockPos pairPos : originBlockEntity.getLinePairs()) {
                if(level.getBlockEntity(pairPos) instanceof WrittenBoardBlockEntity childEntity){
                    List<blockTravel> lineTravels = new ArrayList<>(getChildWithPassed(level, pos, childEntity, depth + 1, maxDepth));
                    for (blockTravel lineTravel : lineTravels) {
                        lineTravel.passed.add(pos);
                        lineTravel.type.add("line");
                    }
                    blockTravels.addAll(lineTravels);
                }
            }
            if(!blockTravels.isEmpty()){
                return blockTravels;
            }
        }
        return List.of(new blockTravel(pos, List.of(pos), List.of("end")));
    }*/

    // ルートノードからネットワークの形を判定
    /*public static boolean matchesAt(Level level, BlockPos originPos, MagicCircleAssemblyRecipe recipe) {
        final int MAX_DEPTH = 16;
        final int MAX_NODES = 16;
        List<Part> parts = recipe.parts;

        // ルート位置のブロックエンティティを取得
        BlockEntity blockEntity = level.getBlockEntity(originPos);
        if (!(blockEntity instanceof AbstractWrittenBoardBlockEntity originBlockEntity)) {
            return false;
        }

        // ルートじゃないならキャンセル
        if (!originBlockEntity.isRoot(level)) return false;

        // パーツ条件をキーで参照できるようにする
        Map<String, Part> partMap = parts.stream().collect(Collectors.toMap(p -> p.key, p -> p));

        // 円周パーツ条件とノードパーツ条件の配列を分離
        List<Part> circleParts = parts.stream().filter(p -> "circle".equals(p.type)).toList();
        List<Part> nodeParts = parts.stream().filter(p -> "node".equals(p.type)).toList();

        // ルート中心円とノード中心円の配列を分離
        List<Part> originRequiredCircles = circleParts.stream()
            .filter(c -> c.center != null && "ORIGIN".equals(c.center))
            .toList();
        List<Part> nodeRequiredCircles = circleParts.stream()
            .filter(c -> c.center != null && !"ORIGIN".equals(c.center))
            .toList();

        // ルート中心円の円周上ノード配列を作成
        Map<Double, Set<BlockPos>> originRadiusLattices = new HashMap<>();
        // ルートが円を持つ必要がある場合
        if (!originRequiredCircles.isEmpty()) {
            // 実際にある円周を取得
            List<Double> radii = originBlockEntity.getCircleRadii();
            // 円周がないならキャンセル
            if (radii == null || radii.isEmpty()) return false;
            // 円周の数が一致しないならキャンセル
            if(radii.size() != originRequiredCircles.size()) return false;
            // 円周ごとのノード位置配列を作成
            for (double radius : radii) {
                List<BlockPos> lattice = AbstractWrittenBoardBlockEntity.getCircleLatticePos(originPos, radius);
                Set<BlockPos> set = new HashSet<>(lattice);
                originRadiusLattices.put(radius, set);
            }
        }

        // ネットワークに所属するノードを幅優先探索
        Map<Long, BlockPos> candidateMap = new HashMap<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        Map<Long, Integer> depthMap = new HashMap<>();

        // 初期候補: 中心円の円周上ノード
        List<Double> originRadii = originBlockEntity.getCircleRadii();
        if (originRadii != null) {
            for (double r : originRadii) {
                List<BlockPos> lattice = AbstractWrittenBoardBlockEntity.getCircleLatticePos(originPos, r);
                for (BlockPos pos : lattice) {
                    long key = pos.asLong();
                    if (!candidateMap.containsKey(key)) {
                        candidateMap.put(key, pos);
                        queue.add(pos);
                        depthMap.put(key, 0);
                    }
                }
            }
        }

        // ルートが含まれる円周の中心 → 同円周上のノード
        List<BlockPos> circleCenters = originBlockEntity.getCircleCenters();
        if (circleCenters != null) {
            // 円ごとに
            for (BlockPos centerPos : circleCenters) {
                BlockEntity centerBeRaw = level.getBlockEntity(centerPos);
                if (!(centerBeRaw instanceof AbstractWrittenBoardBlockEntity centerBlockEntity)) {
                    continue;
                }
                List<Double> radii = centerBlockEntity.getCircleRadii();
                if (radii == null) {
                    continue;
                }
                for (double r : radii) {
                    List<BlockPos> lattice = AbstractWrittenBoardBlockEntity.getCircleLatticePos(centerPos, r);
                    for (BlockPos p : lattice) {
                        long key = p.asLong();
                        if (!candidateMap.containsKey(key)) {
                            candidateMap.put(key, p);
                            queue.add(p);
                            depthMap.put(key, 0);
                        }
                    }
                }
            }
        }

        // origin の linePairs を追加
        List<BlockPos> originPairs = originBlockEntity.getLinePairs();
        if (originPairs != null) {
            for (BlockPos p : originPairs) {
                long key = p.asLong();
                if (!candidateMap.containsKey(key)) {
                    candidateMap.put(key, p);
                    queue.add(p);
                    depthMap.put(key, 0);
                }
            }
        }

        // BFS（深さ制限、候補数上限で早期停止）
        while (!queue.isEmpty()) {
            BlockPos cur = queue.poll();
            long curKey = cur.asLong();
            int curDepth = depthMap.getOrDefault(curKey, 0);
            if (curDepth >= MAX_DEPTH) continue;

            BlockEntity tbe = level.getBlockEntity(cur);
            if (!(tbe instanceof AbstractWrittenBoardBlockEntity)) continue;
            AbstractWrittenBoardBlockEntity wbe = (AbstractWrittenBoardBlockEntity) tbe;

            List<BlockPos> pairs = wbe.getLinePairs();
            if (pairs != null) {
                for (BlockPos q : pairs) {
                    long qKey = q.asLong();
                    if (!candidateMap.containsKey(qKey)) {
                        candidateMap.put(qKey, q);
                        int nd = curDepth + 1;
                        depthMap.put(qKey, nd);
                        if (nd < MAX_DEPTH) queue.add(q);
                    }
                }
            }

            // 候補数が極端に増えたら打ち切り（安全策）
            if (candidateMap.size() > MAX_NODES * 4) break;
        }

        // 最終候補集合を作成（BlockPos -> AbstractWrittenBoardBlockEntity マップ）
        Map<BlockPos, AbstractWrittenBoardBlockEntity> posToNode = new HashMap<>();
        for (BlockPos p : candidateMap.values()) {
            BlockEntity hbe = level.getBlockEntity(p);
            if (hbe instanceof AbstractWrittenBoardBlockEntity) {
                posToNode.put(p, (AbstractWrittenBoardBlockEntity) hbe);
            }
        }

        // adjacency を事前計算（各ノードが持つ接続先）
        Map<BlockPos, Set<BlockPos>> adjacency = new HashMap<>();
        for (Map.Entry<BlockPos, AbstractWrittenBoardBlockEntity> e : posToNode.entrySet()) {
            BlockPos p = e.getKey();
            AbstractWrittenBoardBlockEntity w = e.getValue();
            Set<BlockPos> adj = new HashSet<>();
            List<BlockPos> pairs = w.getLinePairs();
            if (pairs != null) adj.addAll(pairs);
            adjacency.put(p, adj);
        }

        // 「接続ノード」と「非接続ノード」を分離する（接続を1つ以上持つもののみ全置換対象）
        List<BlockPos> connectedPositions = new ArrayList<>();
        for (Map.Entry<BlockPos, Set<BlockPos>> posSetEntry : adjacency.entrySet()) {
            Set<BlockPos> posSet = posSetEntry.getValue();
            if (posSet != null && !posSet.isEmpty()) connectedPositions.add(posSetEntry.getKey());
        }

        // レシピの nodeParts はすべて contains >= 1 である前提
        if (connectedPositions.size() != nodeParts.size()) return false;

        // ノード数上限チェック
        if (connectedPositions.size() > MAX_NODES) return false;

        // ラベル（JSONキー）リスト（固定順）
        List<String> labels = nodeParts.stream().map(p -> p.key).toList();
        int n = labels.size();

        // 事前次数（degree）チェック：レシピ側の要求次数と候補ノードの次数の分布が一致するか簡易確認
        List<Integer> requiredDegrees = nodeParts.stream()
            .map(p -> p.contains.size())
            .sorted()
            .toList();
        List<Integer> candidateDegrees = new ArrayList<>();
        for (BlockPos p : connectedPositions) {
            candidateDegrees.add(adjacency.getOrDefault(p, Collections.emptySet()).size());
        }
        Collections.sort(candidateDegrees);
        if (!requiredDegrees.equals(candidateDegrees)) return false;

        // 位置リストと次数キャッシュ
        List<BlockPos> positionsList = new ArrayList<>(connectedPositions);
        Map<BlockPos, Integer> posDegree = new HashMap<>();
        for (BlockPos p : positionsList) posDegree.put(p, adjacency.getOrDefault(p, Collections.emptySet()).size());

        // ラベル -> 要求次数
        Map<String, Integer> labelDegree = new HashMap<>();
        for (Part p : nodeParts) labelDegree.put(p.key, p.contains.size());

        // mappingValid の最終チェック（全割当後に呼ぶ）
        java.util.function.Function<Map<String, BlockPos>, Boolean> mappingValid = (mapping) -> {
            // 1) nodeParts の contains（接続）を満たすか
            for (Part nodePart : nodeParts) {
                String label = nodePart.key;
                BlockPos pos = mapping.get(label);
                if (pos == null) return false;
                for (String requiredNeighborLabel : nodePart.contains) {
                    BlockPos neighborPos = mapping.get(requiredNeighborLabel);
                    if (neighborPos == null) return false;
                    Set<BlockPos> adj = adjacency.get(pos);
                    if (adj == null || !adj.contains(neighborPos)) return false;
                }
            }

            // 2) originCircles の検証（各 circle が少なくとも1つの半径格子集合に含まれていること）
            for (Part circle : originRequiredCircles) {
                Set<String> requiredLabels = new HashSet<>(circle.contains);
                boolean circleSatisfied = false;
                for (Map.Entry<Double, Set<BlockPos>> entry : originRadiusLattices.entrySet()) {
                    Set<BlockPos> latticeSet = entry.getValue();
                    boolean allInThisRadius = true;
                    for (String label : requiredLabels) {
                        BlockPos assigned = mapping.get(label);
                        if (assigned == null || !latticeSet.contains(assigned)) {
                            allInThisRadius = false;
                            break;
                        }
                    }
                    if (allInThisRadius) {
                        circleSatisfied = true;
                        break;
                    }
                }
                if (!circleSatisfied) return false;
            }

            // 3) nodeCenteredCircles の検証（中心がノードラベルの場合）
            for (Part circle : nodeRequiredCircles) {
                String centerLabel = circle.center;
                BlockPos centerPos = mapping.get(centerLabel);
                if (centerPos == null) return false;
                AbstractWrittenBoardBlockEntity centerBe = posToNode.get(centerPos);
                if (centerBe == null) return false;
                List<Double> radii = centerBe.getCircleRadii();
                if (radii == null || radii.isEmpty()) return false;

                Set<BlockPos> centerLattice = new HashSet<>();
                for (double r : radii) {
                    List<BlockPos> lattice = AbstractWrittenBoardBlockEntity.getCircleLatticePos(centerPos, r);
                    centerLattice.addAll(lattice);
                }

                for (String label : circle.contains) {
                    BlockPos pos = mapping.get(label);
                    if (pos == null) return false;
                    if (!centerLattice.contains(pos)) return false;
                }
            }

            return true;
        };

        // 再帰バックトラック（部分割当で早期打ち切り）
        boolean[] found = new boolean[] { false };
        boolean[] used = new boolean[positionsList.size()];
        BlockPos[] assign = new BlockPos[n];

        // より強い局所チェックを行うためのヘルパー（割当済み隣接チェックを含む）
        java.util.function.BiFunction<Integer, BlockPos, Boolean> strongLocalCheck = (depth, pos) -> {
            String label = labels.get(depth);
            int reqDeg = labelDegree.getOrDefault(label, 0);
            int actualDeg = posDegree.getOrDefault(pos, 0);
            if (reqDeg > actualDeg) return false;

            Part nodePart = partMap.get(label);
            if (nodePart == null) return false;
            for (String neighborLabel : nodePart.contains) {
                int idx = labels.indexOf(neighborLabel);
                if (idx >= 0 && idx < depth) {
                    BlockPos assignedNeighbor = assign[idx];
                    if (assignedNeighbor == null) return false;
                    Set<BlockPos> adj = adjacency.getOrDefault(pos, Collections.emptySet());
                    if (!adj.contains(assignedNeighbor)) return false;
                }
            }
            return true;
        };

        class Backtrack {
            void dfs(int depth) {
                if (found[0]) return;
                if (depth == n) {
                    Map<String, BlockPos> mapping = new HashMap<>();
                    for (int i = 0; i < n; i++) mapping.put(labels.get(i), assign[i]);
                    if (mappingValid.apply(mapping)) found[0] = true;
                    return;
                }
                for (int i = 0; i < positionsList.size(); i++) {
                    if (used[i]) continue;
                    BlockPos candidate = positionsList.get(i);
                    if (!strongLocalCheck.apply(depth, candidate)) continue;
                    used[i] = true;
                    assign[depth] = candidate;
                    dfs(depth + 1);
                    used[i] = false;
                    assign[depth] = null;
                    if (found[0]) return;
                }
            }
        }

        new Backtrack().dfs(0);
        return found[0];
    }*/

    @Override
    public boolean matches(@NotNull Container container, Level level) {
        if(level.isClientSide()){
            return false;
        }
        ItemStack inputItem = container.getItem(0);
        ResourceLocation triggerIngredient = trigger.blockOrItem;
        if(trigger.type.equals("place_on")){
            if (!(inputItem.getItem() instanceof BlockItem inputBlockItem)) {
                return false;
            }
            // ブロック比較 TODO: ブロックアイテムがないブロックに対応(コンテナ側で、nbtで座標を渡す)
            Block inputBlock = inputBlockItem.getBlock();
            Block triggerBlock = ForgeRegistries.BLOCKS.getValue(triggerIngredient);
            if (triggerBlock == null) {
                return false;
            }
            return inputBlock.equals(triggerBlock);

        }else{
            // 材料比較
            Item triggerItem = ForgeRegistries.ITEMS.getValue(triggerIngredient);
            if (triggerItem == null) {
                return false;
            }
            return inputItem.getItem().equals(triggerItem);

        }
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return false;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        Result blockOrItem = getResult();
        if(blockOrItem.isBlock()){
            Block resultBlock = ForgeRegistries.BLOCKS.getValue(blockOrItem.blockOrItem());
            if(resultBlock == null) {
                return Items.AIR.getDefaultInstance();
            }
            return resultBlock.asItem().getDefaultInstance();
        }else{
            Item resultItem = ForgeRegistries.ITEMS.getValue(blockOrItem.blockOrItem());
            if(resultItem == null) {
                return Items.AIR.getDefaultInstance();
            }
            return resultItem.getDefaultInstance();
        }
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        if(!trigger.type().equals("place_on")){
            Item triggerItem = ForgeRegistries.ITEMS.getValue(trigger.blockOrItem());
            return NonNullList.withSize(1, Ingredient.of(triggerItem));
        }else{
            Block triggerBlock = ForgeRegistries.BLOCKS.getValue(trigger.blockOrItem());
            if(triggerBlock != null){
                Item triggerItem = triggerBlock.asItem();
                if(triggerItem != Items.AIR){
                    return NonNullList.withSize(1, Ingredient.of(triggerItem));
                }
            }
            return NonNullList.withSize(1, Ingredient.EMPTY);
        }
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return recipeId;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public Result getResult() {
        return result;
    }

    /*public List<Part> getParts() {
        return parts;
    }*/
    public NonNullList<Node> getNodes(){
        return nodes;
    }

    public NonNullList<Circle> getCircles(){
        return circles;
    }
}