import sklearn.neural_network

class NeuralNet:
    def __init__(self):
        self.regression = sklearn.neural_network.MLPRegressor(hidden_layer_sizes=100)

    def train(self, X, Y):
        self.regression.fit(X, Y)

    def score(self, X):
        return self.regression.predict(X)

    def set_param(self, param):
        self.regression.set_params(param)

    def get_param(self):
        return self.regression.get_params()