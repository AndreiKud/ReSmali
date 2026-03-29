.class public Lcom/example/smalisample/MainActivity;
.super Landroid/app/Activity;
.source "MainActivity.java"


# direct methods
.method public constructor <init>()V
    .locals 0

    .line 11
    invoke-direct {p0}, Landroid/app/Activity;-><init>()V

    return-void
.end method


# virtual methods
.method f(Landroid/view/View;)V
    .locals 21
    .param p1, "button"    # Landroid/view/View;

    .line 22
    move-object/from16 v0, p0

    const/4 v1, 0x1

    .line 23
    .local v1, "testBoolean":Z
    const/16 v2, 0x11

    .line 24
    .local v2, "testByte":B
    const/16 v3, 0x62

    .line 25
    .local v3, "testChar":C
    const/16 v4, 0x12

    .line 26
    .local v4, "testShort":S
    const/16 v5, 0x3e8

    .line 27
    .local v5, "testInt":I
    const-wide/16 v6, 0x7d0

    .line 28
    .local v6, "testLong":J
    const v8, 0x3f19999a    # 0.6f

    .line 29
    .local v8, "testFloat":F
    const-wide v9, 0x40091eb851eb851fL    # 3.14

    .line 30
    .local v9, "testDouble":D
    const/4 v11, 0x0

    .line 32
    .local v11, "testNull":Ljava/lang/String;
    const/16 v12, 0x64

    .line 33
    .local v12, "exp10":I
    const-string v13, "exp11"

    .line 34
    .local v13, "exp11":Ljava/lang/String;
    new-instance v14, Landroid/widget/FrameLayout;

    invoke-direct {v14, v0}, Landroid/widget/FrameLayout;-><init>(Landroid/content/Context;)V

    .line 36
    .local v14, "exp12":Landroid/widget/FrameLayout;
    const/16 v15, 0x33

    move/from16 v16, v1

    .end local v1    # "testBoolean":Z
    .local v16, "testBoolean":Z
    const/16 v1, 0x34

    move/from16 v17, v2

    .end local v2    # "testByte":B
    .local v17, "testByte":B
    const/16 v2, 0x32

    filled-new-array {v2, v15, v1}, [I

    move-result-object v1

    .line 37
    .local v1, "intArr":[I
    const/4 v2, 0x3

    new-array v15, v2, [J

    fill-array-data v15, :array_52

    .line 38
    .local v15, "longArr":[J
    new-array v2, v2, [D

    fill-array-data v2, :array_62

    .line 39
    .local v2, "doubleArr":[D
    move-object/from16 v18, v1

    .end local v1    # "intArr":[I
    .local v18, "intArr":[I
    invoke-static {v3}, Ljava/lang/Character;->valueOf(C)Ljava/lang/Character;

    move-result-object v1

    move-object/from16 v19, v2

    .end local v2    # "doubleArr":[D
    .local v19, "doubleArr":[D
    invoke-static/range {v17 .. v17}, Ljava/lang/Byte;->valueOf(B)Ljava/lang/Byte;

    move-result-object v2

    move/from16 v20, v3

    .end local v3    # "testChar":C
    .local v20, "testChar":C
    invoke-static {v9, v10}, Ljava/lang/Double;->valueOf(D)Ljava/lang/Double;

    move-result-object v3

    filled-new-array {v1, v2, v3}, [Ljava/lang/Object;

    move-result-object v1

    invoke-virtual {v0, v1}, Lcom/example/smalisample/MainActivity;->f2([Ljava/lang/Object;)V

    .line 40
    return-void

    :array_52
    .array-data 8
        0x35
        0x36
        0x37
    .end array-data

    :array_62
    .array-data 8
        0x404c000000000000L    # 56.0
        0x404c800000000000L    # 57.0
        0x404d000000000000L    # 58.0
    .end array-data
.end method

.method varargs f2([Ljava/lang/Object;)V
    .locals 2
    .param p1, "obj"    # [Ljava/lang/Object;

    .line 43
    invoke-static {p1}, Ljava/util/Arrays;->toString([Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v0

    const/4 v1, 0x0

    invoke-static {p0, v0, v1}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object v0

    invoke-virtual {v0}, Landroid/widget/Toast;->show()V

    .line 44
    return-void
.end method

.method protected onCreate(Landroid/os/Bundle;)V
    .locals 2
    .param p1, "savedInstanceState"    # Landroid/os/Bundle;

    .line 15
    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V

    .line 16
    sget v0, Lcom/example/smalisample/R$layout;->activity_main:I

    invoke-virtual {p0, v0}, Lcom/example/smalisample/MainActivity;->setContentView(I)V

    .line 17
    sget v0, Lcom/example/smalisample/R$id;->btn:I

    invoke-virtual {p0, v0}, Lcom/example/smalisample/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v0

    new-instance v1, Lcom/example/smalisample/MainActivity$$ExternalSyntheticLambda0;

    invoke-direct {v1, p0}, Lcom/example/smalisample/MainActivity$$ExternalSyntheticLambda0;-><init>(Lcom/example/smalisample/MainActivity;)V

    invoke-virtual {v0, v1}, Landroid/view/View;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 18
    const/4 v0, 0x0

    invoke-virtual {p0, v0}, Lcom/example/smalisample/MainActivity;->f(Landroid/view/View;)V

    .line 19
    return-void
.end method
