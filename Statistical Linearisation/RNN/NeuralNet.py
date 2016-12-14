from keras.models import Sequential
from keras.layers import Dense
from keras.layers import LSTM
from keras.layers.embeddings import Embedding
from keras.preprocessing import sequence

class NeuralNet:
    def __init__(self):
        pass
    def create_network(self):
        embedding_vector_length = 32
        model = Sequential()
        model.add(Embedding(sentence, embedding_vector_length, inp))
        model.add(LSTM(100))
