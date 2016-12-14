#include <cstdint>
#include <map>
#include <set>
#include <utility>
#include <vector>

int main() {
	std::map<std::uint64_t, std::uint64_t> map;
	map[0] = 1;
	map[2] = 3;

	std::set<std::uint64_t> set;
	set.insert(0);
	set.insert(1);

	std::pair<std::uint64_t, std::uint64_t> pair;
	pair.first = 0;
	pair.second = 1;

	std::vector<std::uint64_t> vector;
	vector.push_back(0);
	vector.push_back(1);

	return 0;
}
