import {  StyleSheet } from 'react-native';

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center'
  },
  waveContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: 10
  },
  box: {
    width: 80,
    height: 30,
    borderRadius: 15,
    backgroundColor: 'green',
    borderColor: 'blue',
    borderWidth: 2,
    justifyContent: 'center',
    alignItems: 'center',
  },
  boxText: {
    fontSize: 14,
    color: 'white',
  },
  boxContainer: {
    width: '100%',
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'center',
    paddingVertical: 10
  },
  optionText: {
    fontSize: 28,
    color: 'red',
    paddingVertical: 10,
  },
});

export default styles;