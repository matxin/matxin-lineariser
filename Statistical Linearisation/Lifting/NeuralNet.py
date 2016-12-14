from keras.models import Sequential
from keras.models import Dense
import numpy

class NeuralNet():
    def __init__(self):
        self.model = Sequential()
        self.model.add(Dense(10, input_dim=78, init="uniform", activation="relu")) # 78 -> no of features in the table 1 of the article
        self.model.add(Dense(8, init="uniform", activation="relu"))
        self.model.add(Dense(4, init="uniform", activation="sigmoid")) # 0-3 liftings up

        self.model.compile(loss="categorical_crossentropy", optimizer='adam') # see a note on the loss function in the Keras doc

    def train(self, X, Y):
        self.model.fit(X, Y, nb_epoch=50, batch_size=5)

    def predict(self, X):
        return self.model.predict_classes(X)

