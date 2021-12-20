from sklearn.linear_model import LinearRegression
import matplotlib.pyplot as plt
import numpy as np

data_num = [9.975, 49.927, 99.967, 249.940, 599.919]  # 1000行
exec_time = [7.1736108, 35.590795, 69.6585572, 174.060977, 420.4359425]

# exec_time = [7.1662744, 34.4621448, 67.6324068, 177.821615, 432.003427]

# 转换成numpy的ndarray数据格式，n行1列,LinearRegression需要列格式数据，如下：
X_train = np.array(data_num).reshape((len(data_num), 1))
Y_train = np.array(exec_time).reshape((len(exec_time), 1))

lineModel = LinearRegression()
lineModel.fit(X_train, Y_train)

# 用训练后的模型，进行预测
Y_predict = lineModel.predict(X_train)

# coef_是系数，intercept_是截距
a1 = lineModel.coef_[0][0]
b = lineModel.intercept_[0]
print("y=%.4f*x%.4f" % (a1, b))

# 对回归模型进行评分，这里简单使用训练集进行评分，实际很多时候用其他的测试集进行评分
print("得分", lineModel.score(X_train, Y_train))

# 简单画图显示
plt.scatter(data_num, exec_time, c="blue")
plt.plot(X_train, Y_predict, c="red")
plt.show()
